package netty.time.Server;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class TimeServer {
	
	private int port;
	
	public TimeServer(int port) {
		super();
		this.port = port;
	}

	public void startChannel() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        System.out.println("准备运行端口：" + port);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap = serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap = serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            	@Override
            	public void initChannel(SocketChannel ch) throws Exception {
            		ch.pipeline().addLast(new IdleStateHandler(8, 0, 0, TimeUnit.SECONDS));
            		ch.pipeline().addLast(
            				new TimeServerHandle()
                            //new WriteTimeoutHandler(10),
                            //控制写入超时10秒构造参数10表示如果持续10秒钟都没有数据写了，那么就超时。
                            //new ReadTimeoutHandler(10)
                     );
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = serverBootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
	}

	public static void main(String[] args) {
		int port;
		if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
		//创建对象
        TimeTask mr = new TimeTask();
        Thread t = new Thread(mr);
        //启动
        t.start();
        
		TimeServer ts = new TimeServer(port);
		ts.startChannel();      
	}

}
