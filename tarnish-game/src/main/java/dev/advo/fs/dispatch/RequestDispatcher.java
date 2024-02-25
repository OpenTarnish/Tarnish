package dev.advo.fs.dispatch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.jboss.netty.channel.Channel;

import dev.advo.fs.net.ondemand.OnDemandRequest;

/**
 * A class which dispatches requests to worker threads.
 * @author Graham
 */
public final class RequestDispatcher {
	
	/**
	 * The maximum size of a queue before requests are rejected.
	 */
	private static final int MAXIMUM_QUEUE_SIZE = 1024;
	
	/**
	 * A queue for pending 'on-demand' requests.
	 */
	private static final BlockingQueue<ChannelRequest<OnDemandRequest>> onDemandQueue = new PriorityBlockingQueue<ChannelRequest<OnDemandRequest>>();

	/**
	 * Gets the next 'on-demand' request from the queue, blocking if none are
	 * available.
	 * @return The 'on-demand' request.
	 * @throws InterruptedException if the thread is interrupted.
	 */
	static ChannelRequest<OnDemandRequest> nextOnDemandRequest() throws InterruptedException {
		return onDemandQueue.take();
	}
	
	/**
	 * Dispatches an 'on-demand' request.
	 * @param channel The channel.
	 * @param request The request.
	 */
	public static void dispatch(Channel channel, OnDemandRequest request) {
//		if (onDemandQueue.size() >= MAXIMUM_QUEUE_SIZE) {
//			channel.close();
//		}
		onDemandQueue.add(new ChannelRequest<OnDemandRequest>(channel, request));
	}
	
	/**
	 * Default private constructor to prevent instantiation.
	 */
	private RequestDispatcher() {
		
	}

}
