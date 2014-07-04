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
package com.norconex.collector.fs.crawler;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Represents a URL crawling status.
 * @author Pascal Essiembre
 */
public enum CrawlStatus implements Serializable { 
    OK, 
    REJECTED, 
    ERROR, 
    UNMODIFIED, 
    DELETED,
    NOT_FOUND, 
    BAD_STATUS;
    
    private static final int LOGGING_STATUS_PADDING = 10;
    
    private final Logger log;
    CrawlStatus() {
        log = LogManager.getLogger(
                this.getClass().getCanonicalName() + "." + toString());
    }
    
    void logInfo(CrawlFile crawlFile){
        if (log.isInfoEnabled()) {
            log.info(StringUtils.leftPad(
                    crawlFile.getStatus().toString(), LOGGING_STATUS_PADDING)
                  + " > " + crawlFile.getURL());
        }
    }

}