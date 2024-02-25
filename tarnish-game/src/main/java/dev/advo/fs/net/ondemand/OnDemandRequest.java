package dev.advo.fs.net.ondemand;

import dev.advo.fs.fs.FileDescriptor;

public final class OnDemandRequest {

	private final FileDescriptor fileDescriptor;
	
	public OnDemandRequest(FileDescriptor fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}
}
