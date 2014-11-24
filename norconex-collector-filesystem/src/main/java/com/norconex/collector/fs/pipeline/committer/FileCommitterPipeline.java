/* Copyright 2013-2014 Norconex Inc.
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
package com.norconex.collector.fs.pipeline.committer;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.collector.core.pipeline.committer.CommitModuleStage;
import com.norconex.collector.core.pipeline.committer.DocumentChecksumStage;
import com.norconex.collector.fs.doc.IFileDocumentProcessor;
import com.norconex.commons.lang.pipeline.Pipeline;

/**
 * @author Pascal Essiembre
 *
 */
public class FileCommitterPipeline extends Pipeline<DocumentPipelineContext> {

    public FileCommitterPipeline() {
        addStage(new DocumentChecksumStage());
        addStage(new DocumentPostProcessingStage());
        addStage(new CommitModuleStage());
    }
    
    //--- Document Post-Processing ---------------------------------------------
    private static class DocumentPostProcessingStage 
            extends AbstractCommitterStage {
        @Override
        public boolean executeStage(FileCommitterPipelineContext ctx) {
            if (ctx.getConfig().getPostImportProcessors() != null) {
                for (IFileDocumentProcessor postProc :
                        ctx.getConfig().getPostImportProcessors()) {
                    postProc.processDocument(
                            ctx.getCrawler().getFileManager(),
                            ctx.getDocument());
                    ctx.getCrawler().fireCrawlerEvent(
                            CrawlerEvent.DOCUMENT_POSTIMPORTED, 
                            ctx.getCrawlData(), postProc);
                }            
            }
            return true;
        }
    }  
}
