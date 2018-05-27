package netty.time.Server;

import java.util.Iterator;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class TimeTask implements Runnable {
	
	public static final String CHARSET = "UTF-8";

	public void run() {
		 sendTaskLoop:
			 for(;;){
				 System.out.println("task is beginning...");
				 try{
		            Map<String, Object> map = GatewayService.getChannels();
		            Iterator<String> it = map.keySet().iterator();
		            while (it.hasNext()) {
		                String key = it.next();
		                System.out.println("current uuid : " + key);
		                ChannelHandlerContext ctx = (ChannelHandlerContext)map.get(key);
		                if(!ctx.channel().isActive()) {
		                	// 删除已经断开的通道
		                	System.out.println("delete uuid : " + key);
		                	map.remove(key);
		                	GatewayService.removeGatewayChannel(key);
		                	continue;
		                }
//			                logger.info("channel id is: " + key);
//			                logger.info("channel: " + ctx.channel().isActive());
		                byte[] b = new byte[8];
		            	b[0] = (byte) 0xCC;
		            	b[1] = (byte) 0x33;
		            	b[2] = (byte) 0x01;
		            	b[3] = (byte) 0x00;
		            	b[4] = (byte) 0x00;
		            	b[5] = (byte) 0x00;
		            	b[6] = (byte) 0xC3;
		            	b[7] = (byte) 0x3C;			                
		            	final ByteBuf time = ctx.alloc().buffer(b.length);
		                time.writeBytes(b);
		                ctx.writeAndFlush(time); // (3)
		            }
		        }catch(Exception e){
		            break
		            sendTaskLoop;
		        }
		        try {
		            Thread.sleep(5000);
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		    }
	}	

}
