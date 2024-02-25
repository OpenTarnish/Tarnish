package dev.advo.fs.net.ondemand;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import dev.advo.fs.fs.FileDescriptor;

public final class OnDemandResponseEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel c, Object msg) throws Exception {
		if (msg instanceof OnDemandResponse) {
			OnDemandResponse resp = (OnDemandResponse) msg;
			
			FileDescriptor fileDescriptor = resp.getFileDescriptor();
			int fileSize = resp.getFileSize();
			ChannelBuffer chunkData = resp.getChunkData();
			
			ChannelBuffer buf = ChannelBuffers.buffer(7 + chunkData.readableBytes());			
			buf.writeMedium(create(fileDescriptor.getType(), fileDescriptor.getFile()));
			buf.writeInt(fileSize);
			buf.writeBytes(chunkData);
			
			return buf;
		}
		return msg;
	}

	  public static int create(int index, int file) {
		    if ((index & 0xFFFFFFE0) != 0)
		      throw new IllegalArgumentException("invalid index " + index + ":" + file); 
		    if ((file & 0xFFF80000) != 0)
		      throw new IllegalArgumentException("invalid file " + index + ":" + file); 
		    return index & 0x1F | (file & 0x7FFFF) << 5;
		  }
}
