package dev.advo.fs.net.ondemand;


import org.jboss.netty.buffer.ChannelBuffer;

import dev.advo.fs.fs.FileDescriptor;

public final class OnDemandResponse {

	private final FileDescriptor fileDescriptor;
	private final int fileSize;
	private final ChannelBuffer chunkData;
	
	public OnDemandResponse(FileDescriptor fileDescriptor, int fileSize, ChannelBuffer chunkData) {
		this.fileDescriptor = fileDescriptor;
		this.fileSize = fileSize;
		this.chunkData = chunkData;
	}

	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	public int getFileSize() {
		return fileSize;
	}

	public ChannelBuffer getChunkData() {
		return chunkData;
	}

}
