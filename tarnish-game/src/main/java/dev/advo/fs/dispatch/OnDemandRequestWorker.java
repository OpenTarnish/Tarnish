package dev.advo.fs.dispatch;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import dev.advo.fs.fs.FileDescriptor;
import dev.advo.fs.fs.IndexedFileSystem;
import dev.advo.fs.net.ondemand.OnDemandRequest;
import dev.advo.fs.net.ondemand.OnDemandResponse;

/**
 * A worker which services 'on-demand' requests.
 * @author Graham
 */
public final class OnDemandRequestWorker implements Runnable {

	/**
	 * The resource provider.
	 */
	private final IndexedFileSystem provider;
	/**
	 * An object used for locking checks to see if the worker is running.
	 */
	private final Object lock = new Object();
	/**
	 * A flag indicating if the worker should be running.
	 */
	private boolean running = true;

	/**
	 * Creates the 'on-demand' request worker.
	 * @param fs The file system.
	 */
	public OnDemandRequestWorker(IndexedFileSystem fs) {
		this.provider = fs;
	}

	protected ChannelRequest<OnDemandRequest> nextRequest() throws InterruptedException {
		return RequestDispatcher.nextOnDemandRequest();
	}

	/*
	 * In this request, the server will receive a byte of 1 followed by a big endian medium int 
	 * that contains the index/file being requested. The server then responds with a big endian medium 
	 * for index/file, a big endian int for the size of the data and then a byte[] with the file data.

	 */
	protected void service(IndexedFileSystem fs, Channel channel, OnDemandRequest request) throws IOException {
		FileDescriptor desc = request.getFileDescriptor();
		System.err.println("Serving file: " + desc.toString());

		ByteBuffer buf = fs.getFile(desc);
		int bufLen = buf.remaining();
		int length = bufLen;
		
		byte[] tmp = new byte[bufLen];
		buf.get(tmp, 0, tmp.length);
		ChannelBuffer chunkData = ChannelBuffers.wrappedBuffer(tmp, 0, bufLen);
		
		OnDemandResponse response = new OnDemandResponse(desc, length, chunkData);
		channel.write(response);
	}

	/**
	 * Stops this worker. The worker's thread may need to be interrupted.
	 */
	public final void stop() {
		synchronized (lock) {
			running = false;
		}
	}

	@Override
	public final void run() {
		while (true) {
			synchronized (lock) {
				if (!running) {
					break;
				}
			}

			ChannelRequest<OnDemandRequest> request;
			try {
				request = nextRequest();
			} catch (InterruptedException e) {
				continue;
			}

			Channel channel = request.getChannel();

			try {
				service(provider, channel, request.getRequest());
			} catch (IOException e) {
			//	System.err.println("Error serving file: " + request.getRequest().getFileDescriptor().toString());
			//	e.printStackTrace();
			//	channel.close();
			}
		}
	}
}
