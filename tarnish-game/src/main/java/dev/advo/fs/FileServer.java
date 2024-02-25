package dev.advo.fs;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import dev.advo.fs.dispatch.RequestWorkerPool;
import dev.advo.fs.net.FileServerHandler;
import dev.advo.fs.net.NetworkConstants;
import dev.advo.fs.net.OnDemandPipelineFactory;

public final class FileServer {
	private static final Logger logger = Logger.getLogger(FileServer.class.getName());

	public static void main(String[] args) {
		try {
			new FileServer().start();
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Error starting server.", t);
		}
	}
	
	private final ExecutorService service = Executors.newCachedThreadPool();
	
	private final RequestWorkerPool pool = new RequestWorkerPool();
	private final FileServerHandler handler = new FileServerHandler();
	

	private final Timer timer = new HashedWheelTimer();
	
	public void start() throws Exception {
		logger.info("Starting workers...");
		pool.start();
		
		logger.info("Starting services...");
		start("ondemand", new OnDemandPipelineFactory(handler, timer), NetworkConstants.SERVICE_PORT);
		
		logger.info("Ready for connections.");
	}

	private void start(String name, ChannelPipelineFactory pipelineFactory, int port) {
		SocketAddress address = new InetSocketAddress(port);
		
		logger.info("Binding " + name + " service to " + address + "...");
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.setFactory(new NioServerSocketChannelFactory(service, service));
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.bind(address);
	}

}
