 package netty.time.Server;

import java.util.Map;

import com.sun.xml.internal.ws.util.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

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
			
			// 如果有12个字节长并且头两个字节是id，则是4G模块的注册信息，加入到GatewayService里面去
			if(str.length() == 12 && str.indexOf("ht") == 0) {
				System.out.println("设备发来的注册报文------" + str);
		        System.out.println("a new device connect come in remote ip: " + ctx.channel().remoteAddress());
				GatewayService.addGatewayChannel(str, ctx);
				id = str;
				type = "ht";
			} else if(str.length() == 12 && str.indexOf("wb") == 0){
				// 如果头2个字节是wb则是web端的注册信息
				System.out.println("WEB发来的注册报文------" + str);
		        System.out.println("a new device connect come in remote ip: " + ctx.channel().remoteAddress());
		        GatewayService.addGatewayChannel(str, ctx);
		        id = str;
				type = "wb";
		        // 反馈信息给服务器
            	ctx.writeAndFlush(Unpooled.copiedBuffer("Server ok!!!".getBytes()));    
			} else if(b[0] == 0x33 && b.length == 8) {
				// 设备发来的状态信息
				System.out.println("设备 "+id+" 发来的状态信息-----" + printHexString(b));
			} else if(str.length() == 20 && str.indexOf("ht") == 0){
				// web发来的指令，前12个字节是id，后面8个字节是指令
				System.out.println("web端 "+id+" 发来的指令-----" + str);
				String st = str.substring(0,12);
				byte[] bt = new byte[8];
				System.arraycopy(b, 12, bt, 0, 8);
				System.out.println("向设备 " + st + "发送指令-----" + printHexString(bt));
				sendInstruce(st, bt);
			} else {
				System.out.println("收到发来的报文-----" + str);
				System.out.println("收到发来的报文-----" + printHexString(b));
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
    
    public void sendInstruce(String key, byte[] b) {
    	Map<String, Object> map = GatewayService.getChannels();
		ChannelHandlerContext ctx = (ChannelHandlerContext)map.get(key);
		if (ctx == null) {
			System.out.println("通道不存在-----" + key);
			return;
		}
        if(!ctx.channel().isActive()) {
        	// 删除已经断开的通道
        	System.out.println("通道已经断开，发送失败。delete id : " + key);
        	map.remove(key);
        	GatewayService.removeGatewayChannel(key);
        } else {
        	final ByteBuf time = ctx.alloc().buffer(b.length);
            time.writeBytes(b);
            ctx.writeAndFlush(time); 
        }
    }
}
