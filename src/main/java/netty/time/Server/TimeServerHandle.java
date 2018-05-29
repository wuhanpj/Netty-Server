 package netty.time.Server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class TimeServerHandle extends ChannelHandlerAdapter {
	
	private int lossConnectCount = 0;
	private String id; // 设备编号
	private String type; // 客户端类型：ht设备端,wb Web端

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
			// 如果有12个字节长并且头两个字节是id，则是4G模块的注册信息，加入到GatewayService里面去
			if(str.length() == 12 && str.indexOf("ht") == 0) {
				GatewayService.addGatewayChannel(str, ctx);
				id = str;
				type = "ht";
		        System.out.println("a new device connect come in: " + str);
		        System.out.println("a new device connect come in remote ip: " + ctx.channel().remoteAddress());
			} else if(str.length() == 12 && str.indexOf("wb") == 0){
				// 如果头2个字节是wb则是web端的注册信息
				id = str;
				type = "wb";
		        System.out.println("a new web connect come in: " + str);
		        System.out.println("a new web connect come in remote ip: " + ctx.channel().remoteAddress());
		        // 反馈信息给服务器
                //ByteBuf buf = ctx.alloc().buffer(8);
            	//buf.writeBytes("ok!!!".getBytes());
            	ctx.writeAndFlush(Unpooled.copiedBuffer("ok!!!".getBytes()));    
			} else if(b[0] == 0x33) {
				// 设备发来的状态信息
				System.out.println("设备发来的状态信息-----" + printHexString(b));
			} else {
				// web发来的指令，前12个字节是id，后面8个字节是指令
				System.out.println("web端发来的指令-----" + str);
				
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
	
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("已经10秒未收到客户端的消息了！");
        if (evt instanceof IdleStateEvent && type.equals("ht")){
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
