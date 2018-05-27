package netty.time.Server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;

public class GatewayService {

	private static Map<String, Object> map = new ConcurrentHashMap<String, Object>();
    
    public static void addGatewayChannel(String id, ChannelHandlerContext gateway_ctx){
        map.put(id, gateway_ctx);
    }
    
    public static Map<String, Object> getChannels(){
        return map;
    }

    public static Object getGatewayChannel(String id){
        return map.get(id);
    }
    
    public static void removeGatewayChannel(String id){
        map.remove(id);
    }
}
