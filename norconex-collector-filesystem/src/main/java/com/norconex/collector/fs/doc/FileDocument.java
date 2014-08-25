/* Copyright 2010-2013 Norconex Inc.
 * 
 * This file is part of Norconex HTTP Collector.
 * 
 * Norconex HTTP Collector is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex HTTP Collector is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex HTTP Collector. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.fs.doc;

import java.io.File;
import java.io.Serializable;

public class FileDocument implements Serializable {

    private static final long serialVersionUID = -6020931145385049064L;

    private final String referenceId;
	private final File localFile;
	private final FileMetadata metadata;

	public FileDocument(String referenceId, File localFile) {
		super();
		this.referenceId = referenceId;
		this.localFile = localFile;
		this.metadata = new FileMetadata(referenceId);
	}

	public String getReference() {
		return referenceId;
	}

	public File getLocalFile() {
		return localFile;
	}

	public FileMetadata getMetadata() {
		return metadata;
	}
}
