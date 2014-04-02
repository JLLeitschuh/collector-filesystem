/* Copyright 2010-2014 Norconex Inc.
 * 
 * This file is part of Norconex Filesystem Collector.
 * 
 * Norconex Filesystem Collector is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Filesystem Collector is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Filesystem Collector. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.fs.crawler.FilesystemCrawler;
import com.norconex.collector.fs.crawler.FilesystemCrawlerConfig;
import com.norconex.commons.lang.EqualsUtil;
import com.norconex.jef.AsyncJobGroup;
import com.norconex.jef.IJob;
import com.norconex.jef.JobRunner;
import com.norconex.jef.log.FileLogManager;
import com.norconex.jef.progress.JobProgressPropertiesFileSerializer;
import com.norconex.jef.suite.FileStopRequestHandler;
import com.norconex.jef.suite.IJobSuiteFactory;
import com.norconex.jef.suite.JobSuite;
 
/**
 * Main application class. In order to use it properly, you must first configure
 * it, either by providing a populated instance of {@link FilesystemCollectorConfig},
 * or by XML configuration, loaded using {@link FilesystemCollectorConfigLoader}.
 * Instances of this class can hold several crawler, running at once.
 * This is convenient when there are configuration setting to be shared amongst
 * crawlers.  When you have many crawler jobs defined that have nothing
 * in common, it may be best to configure and run them separately, to facilitate
 * troubleshooting.  There is no fair rule for this, experimenting with your
 * target sites will help you.
 * @author Pascal Dimassimo
 */
@SuppressWarnings("nls")
public class FilesystemCollector implements IJobSuiteFactory {

	private static final Logger LOG = LogManager.getLogger(FilesystemCollector.class);
	
	private static final String ARG_ACTION = "action";
    private static final String ARG_ACTION_START = "start";
    private static final String ARG_ACTION_RESUME = "resume";
    private static final String ARG_ACTION_STOP = "stop";
    private static final String ARG_CONFIG = "config";
    private static final String ARG_VARIABLES = "variables";
	
    private File configurationFile;
    private File variablesFile;
    private FilesystemCrawler[] crawlers;
    private FilesystemCollectorConfig collectorConfig;
    
    /**
     * Creates a non-configured HTTP collector.
     */
    public FilesystemCollector() {
        super();
    }
    /**
     * Creates an HTTP Collector configured using the provided
     * configuration fine and variable files.  Sample configuration files
     * and documentation on configuration options and the differences 
     * between a variables file and configuration are found on 
     * the HTTP Collector web site.
     * @param configFile a configuration file
     * @param variablesFile a variables file
     */
	public FilesystemCollector(File configFile, File variablesFile) {
	    this.configurationFile = configFile;
	    this.variablesFile = variablesFile;
	}
	/**
	 * Creates and configure an HTTP Collector with the provided
	 * configuration.
	 * @param collectorConfig HTTP Collector configuration
	 */
    public FilesystemCollector(FilesystemCollectorConfig collectorConfig) {
        this.collectorConfig = collectorConfig;
    }
	/**
	 * Gets the configuration file used to initialize this collector.
	 * @return XML configuration file
	 */
	public File getConfigurationFile() {
        return configurationFile;
    }
	/**
	 * Sets the configuration file used to initialize this collector.
	 * @param configurationFile XML configuration file
	 */
    public void setConfigurationFile(File configurationFile) {
        this.configurationFile = configurationFile;
    }
    /**
     * Gets the file holding configuration variables, or <code>null</code>
     * if none.
     * @return variables file
     */
    public File getVariablesFile() {
        return variablesFile;
    }
    /**
     * Sets the file holding configuration variables, or <code>null</code>
     * if none. 
     * @param variablesFile variables file
     */
    public void setVariablesFile(File variablesFile) {
        this.variablesFile = variablesFile;
    }
    /**
     * Gets the HTTP crawlers to be run by this collector.
     * @return HTTP crawlers
     */
    public FilesystemCrawler[] getCrawlers() {
        return crawlers;
    }
    /**
     * Sets the HTTP crawlers to be run by this collector.
     * @param crawlers HTTP Crawler
     */
    public void setCrawlers(FilesystemCrawler[] crawlers) {
        this.crawlers = ArrayUtils.clone(crawlers);
    }
    
    /**
     * Invokes the Filesystem Collector from the command line.  
     * @param args Invoke it once without any arguments to get a 
     *    list of command-line options.
     */
	public static void main(String[] args) {
	    CommandLine cmd = parseCommandLineArguments(args);
        String action = cmd.getOptionValue(ARG_ACTION);
        File configFile = new File(cmd.getOptionValue(ARG_CONFIG));
        File varFile = null;
        if (cmd.hasOption(ARG_VARIABLES)) {
            varFile = new File(cmd.getOptionValue(ARG_VARIABLES));
        }
	    
        try {
            FilesystemCollector conn = new FilesystemCollector(configFile, varFile);
    	    if (ARG_ACTION_START.equalsIgnoreCase(action)) {
    	        conn.crawl(false);
    	    } else if (ARG_ACTION_RESUME.equalsIgnoreCase(action)) {
                conn.crawl(true);
    	    } else if (ARG_ACTION_STOP.equalsIgnoreCase(action)) {
    	        conn.stop();
    	    }
        } catch (Exception e) {
        	File errorFile = new File(
        			"./error-" + System.currentTimeMillis() + ".log");
        	System.err.println("\n\nAn ERROR occured:\n\n"
                  + e.getLocalizedMessage());
        	System.err.println("\n\nDetails of the error has been stored at: "
        			+ errorFile.getAbsolutePath() + "\n\n");
        	try {
        		PrintWriter w = new PrintWriter(errorFile);
				e.printStackTrace(w);
				w.flush();
				w.close();
			} catch (FileNotFoundException e1) {
				throw new FilesystemCollectorException(
				        "Cannot write error file.", e1);
			}
        }
	}

    /**
     * Launched all crawlers defined in configuration.
     * @param resumeNonCompleted whether to resume where previous crawler
     *        aborted (if applicable) 
     */
    public void crawl(boolean resumeNonCompleted) {
        JobSuite suite = createJobSuite();
        JobRunner jobRunner = new JobRunner();
        jobRunner.runSuite(suite, resumeNonCompleted);
    }

    /**
     * Stops a running instance of this HTTP Collector.
     */
    public void stop() {
        JobSuite suite = createJobSuite();
        ((FileStopRequestHandler) 
                suite.getStopRequestHandler()).fireStopRequest();
    }
    
    
    @Override
    public JobSuite createJobSuite() {
        if (collectorConfig == null) {
            try {
                collectorConfig = FilesystemCollectorConfigLoader.loadCollectorConfig(
                        getConfigurationFile(), getVariablesFile());
            } catch (Exception e) {
                throw new FilesystemCollectorException(e);
            }
        }
        if (collectorConfig == null) {
        	throw new FilesystemCollectorException(
        			"Configuration file does not exists: "
        			+ getConfigurationFile());
        }
        FilesystemCrawlerConfig[] configs = collectorConfig.getCrawlerConfigs();
        crawlers = new FilesystemCrawler[configs.length];
        for (int i = 0; i < configs.length; i++) {
            FilesystemCrawlerConfig crawlerConfig = configs[i];
            crawlers[i] = new FilesystemCrawler(crawlerConfig);
        }

        IJob rootJob = null;
        if (crawlers.length > 1) {
            rootJob = new AsyncJobGroup(
                    collectorConfig.getId(), crawlers
            );
        } else if (crawlers.length == 1) {
            rootJob = crawlers[0];
        }
        
        JobSuite suite = new JobSuite(
                rootJob, 
                new JobProgressPropertiesFileSerializer(
                        collectorConfig.getProgressDir()),
                new FileLogManager(collectorConfig.getLogsDir()),
                new FileStopRequestHandler(collectorConfig.getId(), 
                        collectorConfig.getProgressDir()));
        LOG.info("Suite of " + crawlers.length + " HTTP crawler jobs created.");
        return suite;
    }

    private static CommandLine parseCommandLineArguments(String[] args) {
        Options options = new Options();
        options.addOption("c", ARG_CONFIG, true, 
                "Required: Filesystem Collector configuration file.");
        options.addOption("v", ARG_VARIABLES, true, 
                "Optional: variable file.");
        options.addOption("a", ARG_ACTION, true, 
                "Required: one of start|resume|stop");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
            if(!cmd.hasOption(ARG_CONFIG) || !cmd.hasOption(ARG_ACTION)
                    || EqualsUtil.equalsNone(cmd.getOptionValue(ARG_ACTION),
                        ARG_ACTION_START, ARG_ACTION_RESUME, ARG_ACTION_STOP)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "collector-fs[.bat|.sh]", options );
                System.exit(-1);
            }
        } catch (ParseException e) {
            System.err.println("Could not parse arguments.");
            e.printStackTrace(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "collector-fs[.bat|.sh]", options );
            System.exit(-1);
        }
        return cmd;
    }
}
