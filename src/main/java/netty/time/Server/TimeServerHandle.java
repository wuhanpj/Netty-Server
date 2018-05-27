package netty.time.Server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class TimeServerHandle extends ChannelHandlerAdapter {

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws InterruptedException {
		
		String uuid = ctx.channel().id().asLongText();
        GatewayService.addGatewayChannel(uuid, ctx);
        System.out.println("a new connect come in: " + uuid);
        System.out.println("a new connect come in remote ip: " + ctx.channel().remoteAddress());
		
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			ByteBuf in = (ByteBuf) msg;
			byte[] b = new byte[in.readableBytes()];
			in.readBytes(b);
			System.out.println(printHexString(b));
			
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
	
	 //将指定byte数组以16进制的形式打印到控制台 
    public String printHexString( byte[] b) { 
    	StringBuffer bf = new StringBuffer();
    	for (int i = 0; i < b.length; i++) { 
    		String hex = Integer.toHexString(b[i] & 0xFF); 
    		if (hex.length() == 1) { 
    			hex = '0' + hex; 
    		} 
    		bf.append(hex.toUpperCase() + " "); 
    	} 
    	return bf.toString();
    }
}
