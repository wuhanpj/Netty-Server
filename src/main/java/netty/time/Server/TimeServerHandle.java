package netty.time.Server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class TimeServerHandle extends ChannelHandlerAdapter {
	
	private int lossConnectCount = 0;

//	@Override
//	public void channelActive(final ChannelHandlerContext ctx) throws InterruptedException {
//		
//		String uuid = ctx.channel().id().asLongText();
//        GatewayService.addGatewayChannel(uuid, ctx);
//        System.out.println("a new connect come in: " + uuid);
//        System.out.println("a new connect come in remote ip: " + ctx.channel().remoteAddress());
//		
//	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		lossConnectCount = 0;
		try {
			ByteBuf in = (ByteBuf) msg;
			String str = in.toString(CharsetUtil.UTF_8);
			byte[] b = new byte[in.readableBytes()];
			in.readBytes(b);
			System.out.println("收到报文16进制-----" + printHexString(b));			
			System.out.println("收到报文------" + str);
			// 如果有12个字节长并且头两个字节是id，则是  注册信息，加入到GatewayService里面去
			if(str.length() == 12 && str.indexOf("ht") == 0) {
				GatewayService.addGatewayChannel(str, ctx);
		        System.out.println("a new connect come in: " + str);
		        System.out.println("a new connect come in remote ip: " + ctx.channel().remoteAddress());
			} else {
				
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("已经10秒未收到客户端的消息了！");
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.READER_IDLE){
                lossConnectCount++;
                if (lossConnectCount>3){
                    System.out.println("关闭这个不活跃通道！");
                    ctx.channel().close().sync();
                }
            }
        }else {
            super.userEventTriggered(ctx,evt);
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
