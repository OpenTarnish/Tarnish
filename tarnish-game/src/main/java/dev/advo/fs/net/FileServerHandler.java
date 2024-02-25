package dev.advo.fs.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import dev.advo.fs.dispatch.RequestDispatcher;
import dev.advo.fs.net.ondemand.OnDemandRequest;
import dev.advo.fs.net.service.ServiceRequest;
import dev.advo.fs.net.service.ServiceResponse;

public final class FileServerHandler extends IdleStateAwareChannelUpstreamHandler {
	
	private static final Logger logger = Logger.getLogger(FileServerHandler.class.getName());
	
	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
//		System.out.println("Closing channel due to idle msg received.");
//		e.getChannel().close();
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();		
		if (msg instanceof ServiceRequest) {
			ServiceRequest request = (ServiceRequest) msg;
			if (request.invalid()) {
			//	System.out.println("Closing channel due to incorrect msg received. ");
			//	e.getChannel().close();
			} else {
				e.getChannel().write(new ServiceResponse());
			}
		} else if (msg instanceof OnDemandRequest) {
			RequestDispatcher.dispatch(e.getChannel(), (OnDemandRequest) msg);
		} else {
			throw new Exception("unknown message type");
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
//		logger.log(Level.SEVERE, "Exception occured, closing channel...", e.getCause());
//		System.out.println("Closing channel due to incorrect msg received.");
//		e.getChannel().close();
	}

}
