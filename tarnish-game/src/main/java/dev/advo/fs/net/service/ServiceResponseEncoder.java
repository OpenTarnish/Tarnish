package dev.advo.fs.net.service;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import dev.advo.fs.fs.IndexedFileSystem;

public final class ServiceResponseEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel c, Object msg) throws Exception {
		if (msg instanceof ServiceResponse) {
			byte[] bytes = IndexedFileSystem.bytes;
			ChannelBuffer buf = ChannelBuffers.buffer(bytes.length+4);
			buf.writeInt(bytes.length);
			buf.writeBytes(bytes);
		
			System.out.println("Responding with packet to client with size " + bytes.length);
			return buf;
		}
		return msg;
	}
}
