package dev.advo.fs.net.ondemand;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import dev.advo.fs.fs.FileDescriptor;

public final class OnDemandRequestDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel c, ChannelBuffer buf) throws Exception {
//		System.out.println("corrupted request" + buf.readableBytes());//lmfao. 
//		for(int i = 0; i < buf.readableBytes(); i++)
//			System.out.println("corrupted request " + i + " " + buf.readByte());
		if (buf.readableBytes() >= 4) {
			int check = buf.readUnsignedByte();
			if(check != 1) {
				System.out.println("corrupted request " + check);

				//After the client submits type 0 for VT. All requestions should be 1.
				return null;
			}
			int hashed = buf.readMedium();
			int type = index(hashed);
			int file = file(hashed);
			System.out.println("requesting file " + type + " - " + file);

			FileDescriptor desc = new FileDescriptor(type, file);
			
			return new OnDemandRequest(desc);
		} else 
		return null;
	}

	  public static int index(int pair) {
		    return pair & 0x1F;
		  }
		  
		  public static int file(int pair) {
		    return pair >>> 5 & 0x7FFFF;
		  }
}
