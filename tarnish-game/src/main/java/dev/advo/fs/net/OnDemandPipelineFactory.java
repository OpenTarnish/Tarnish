package dev.advo.fs.net;


import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

import dev.advo.fs.net.ondemand.OnDemandRequestDecoder;
import dev.advo.fs.net.ondemand.OnDemandResponseEncoder;
import dev.advo.fs.net.service.ServiceRequestDecoder;
import dev.advo.fs.net.service.ServiceResponseEncoder;

public final class OnDemandPipelineFactory implements ChannelPipelineFactory {

	private final FileServerHandler handler;
	private final Timer timer;
	
	public OnDemandPipelineFactory(FileServerHandler handler, Timer timer) {
		this.handler = handler;
		this.timer = timer;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		// decoders
		pipeline.addLast("serviceDecoder", new ServiceRequestDecoder());
		pipeline.addLast("decoder", new OnDemandRequestDecoder());
		
		// encoders
		pipeline.addLast("serviceEncoder", new ServiceResponseEncoder());
		pipeline.addLast("encoder", new OnDemandResponseEncoder());
		
		// handler
		pipeline.addLast("timeout", new IdleStateHandler(timer, NetworkConstants.IDLE_TIME, 0, 0));
		pipeline.addLast("handler", handler);
		
		return pipeline;
	}

}
