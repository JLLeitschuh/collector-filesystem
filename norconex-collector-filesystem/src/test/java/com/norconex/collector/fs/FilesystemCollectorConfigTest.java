/* Copyright 2017-2018 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.collector.fs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.event.ICrawlerEventListener;
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.collector.core.filter.impl.RegexMetadataFilter;
import com.norconex.collector.fs.crawler.FilesystemCrawlerConfig;
import com.norconex.collector.fs.crawler.IStartPathsProvider;
import com.norconex.collector.fs.crawler.MockStartPathsProvider;
import com.norconex.collector.fs.option.impl.GenericFilesystemOptionsProvider;
import com.norconex.committer.core.impl.FileSystemCommitter;
import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.log.CountingConsoleAppender;


/**
 * @author Pascal Essiembre
 */
public class FilesystemCollectorConfigTest {

    @Test
    public void testWriteRead() throws IOException {
        FilesystemCollectorConfig config = new FilesystemCollectorConfig();
        config.setId("test-fs-collector");
        
        FilesystemCrawlerConfig crawlerCfg = new FilesystemCrawlerConfig();
        crawlerCfg.setId("myCrawler");
        crawlerCfg.setOptionsProvider(new GenericFilesystemOptionsProvider());
        crawlerCfg.setPathsFiles(new String[] {"/path/file1", "/path/file2"});
        crawlerCfg.setCommitter(new FileSystemCommitter());
        crawlerCfg.setStartPaths(new String[] {"/start/file1", "/start/file2"});
        RegexMetadataFilter metaFilter = new RegexMetadataFilter();
        crawlerCfg.setStartPathsProviders(new MockStartPathsProvider());
        metaFilter.setField("field");
        metaFilter.setRegex("potato");
        crawlerCfg.setMetadataFilters(metaFilter);
        config.setCrawlerConfigs(new ICrawlerConfig[] {crawlerCfg});
        
        System.out.println("Writing/Reading this: " + config);
        XMLConfigurationUtil.assertWriteRead(config);
    }
    
    
    @Test
    public void testValidation() throws IOException {
        CountingConsoleAppender appender = new CountingConsoleAppender();
        appender.startCountingFor(XMLConfigurationUtil.class, Level.WARN);
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "/validation/collector-fs-full.xml"))) {
            XMLConfigurationUtil.loadFromXML(
                    new FilesystemCollectorConfig(), r);
        } finally {
            appender.stopCountingFor(XMLConfigurationUtil.class);
        }
        Assert.assertEquals("Validation warnings/errors were found.", 
                0, appender.getCount());
    }
    
    // test for: https://github.com/Norconex/collector-filesystem/issues/29
    @SuppressWarnings("unchecked")
    @Test
    public void testNulls() throws IOException {
        FilesystemCollectorConfig config = new FilesystemCollectorConfig();
        config.setId("test-fs-collector");
        
        FilesystemCrawlerConfig crawlerCfg = new FilesystemCrawlerConfig();
        crawlerCfg.setCrawlDataStoreFactory(null);
        crawlerCfg.setCommitter(null);
        crawlerCfg.setCrawlerListeners((ICrawlerEventListener) null);
        crawlerCfg.setDocumentChecksummer(null);
        crawlerCfg.setDocumentFetcher(null);
        crawlerCfg.setDocumentFilters((IDocumentFilter) null);
        crawlerCfg.setId("testing Nulls");
        crawlerCfg.setImporterConfig(null);
        crawlerCfg.setStartPaths(null);
        crawlerCfg.setStartPathsProviders();
        crawlerCfg.setMetadataChecksummer(null);
        crawlerCfg.setMetadataFetcher(null);
        crawlerCfg.setMetadataFilters((IMetadataFilter) null);
        crawlerCfg.setOptionsProvider(null);
        crawlerCfg.setOrphansStrategy(null);
        crawlerCfg.setPostImportProcessors(null);
        crawlerCfg.setPathsFiles(null);
        crawlerCfg.setPreImportProcessors(null);
        crawlerCfg.setReferenceFilters((IReferenceFilter) null);
        crawlerCfg.setSpoiledReferenceStrategizer(null);
        crawlerCfg.setStartPaths(null);
        crawlerCfg.setStartPathsProviders((IStartPathsProvider) null);
        crawlerCfg.setStopOnExceptions((Class<? extends Exception>) null);
        crawlerCfg.setWorkDir(null);
        config.setCrawlerConfigs(crawlerCfg);

        // Should not throw NPE:
        config.saveToXML(new StringWriter());
    }
}
