package umbrella.sun.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by paladintyrion on 16/12/19.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 *
 */
public abstract class AbstractNettyServer implements INettyServer {

    /* boss线程工厂 */
    final private ThreadFactory threadBossFactory;

    /* workers线程工厂 */
    final private ThreadFactory threadWorkersFactory;

    /* 标识服务器是否已经关闭 */
    private volatile boolean close;

    /* netty服务引导 */
    private ServerBootstrap servBootstrap;

    /* boss轮询池，用于接受请求和转发 */
    private EventLoopGroup boss;

    /* workers工作池，用于处理具体的channel */
    private EventLoopGroup workers;

    /* 默认工作线程，用于最后server退出清理 */
//    private EventExecutor defaultExecutor;

    /* 服务提供的服务绑定地址 */
    private InetSocketAddress serverIpAddr;

    /* channelfuture */
    private ChannelFuture sync;

    private List<ChannelHandler> handlers;

    /* channel group未来释放所有连接的channel */
//    private ChannelGroup allChannels;

    /* 构造函数 */
    public AbstractNettyServer(InetSocketAddress serverIpAddr, List<ChannelHandler> handlers) {
        this.serverIpAddr = serverIpAddr;
        this.threadBossFactory = new ThreadFactoryBuilder()
            .setDaemon(true).setNameFormat("Server-boss-%d").build();
        this.threadWorkersFactory = new ThreadFactoryBuilder()
            .setDaemon(true).setNameFormat("Server-workers-%d").build();
//        defaultExecutor = GlobalEventExecutor.INSTANCE;

        this.handlers = new ArrayList<ChannelHandler>();
        if (handlers != null) {
            Collections.copy(this.handlers, handlers);
        }
        this.close = true;
    }

    /* 构造函数 */
    public AbstractNettyServer(InetSocketAddress serverIpAddr) {
        this(serverIpAddr, null);
    }

    /* 只提供端口的构造函数 */
    public AbstractNettyServer(int port) {
        this(new InetSocketAddress(port));
    }

    @Override
    public void start() throws Exception {
        if (servBootstrap == null) {
            System.out.println("**********启动NettyServer服务！！！**********");
            init();
        }

        // 绑定端口，同步等待成功
        sync = this.servBootstrap.bind().sync();
        // 等待服务端监听端口关闭
        sync.channel().closeFuture().addListeners(ChannelFutureListener.CLOSE);
        // 放到待关闭池里
//        allChannels.add(sync.channel());
        // 关闭close标志位，服务正常提供
        this.close = false;
    }

    @Override
    public void init() {
        try {
            boss = new NioEventLoopGroup(1, this.threadBossFactory);
            workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, this.threadWorkersFactory);

//            allChannels = new DefaultChannelGroup(defaultExecutor);

            servBootstrap = new ServerBootstrap();
            servBootstrap.group(boss, workers).channel(NioServerSocketChannel.class)
                //当服务端请求处理线程全满时，队列最大请求连接数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //不启用心跳保活
                .option(ChannelOption.SO_KEEPALIVE, false)
                //recv缓冲区buf的大小
                .option(ChannelOption.SO_RCVBUF, 65535)
                //send缓冲区buf的大小
                .option(ChannelOption.SO_SNDBUF, 65535)
                //workers loop不使用nagle算法，有消息就发
                .childOption(ChannelOption.TCP_NODELAY, true)
                //boss loop日志记录级别
                .handler(new LoggingHandler(LogLevel.INFO))
                //boss loop绑定服务地址
                .localAddress(serverIpAddr)
                //workers loop设置channel handler
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pp = ch.pipeline();
                        pp.addLast(new LoggingHandler(LogLevel.INFO))
                            .addLast(new EchoServerHandler());

                        //TODO: 此处应有序列化监听的处理
                        filterAndAddHandler(pp);
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 过滤并添加handler */
    public void filterAndAddHandler(ChannelPipeline pipeline) {
        for (ChannelHandler handler : handlers) {
            if (filterHandler(handler)) {
                pipeline.addLast(handler);
            }
        }
    }

    /* handler过滤器 */
    public abstract boolean filterHandler(ChannelHandler h);

    @Override
    public void close() {
        if (isClose()) {
            return;
        }
        close = true;
        try {
            workers.shutdownGracefully();
            boss.shutdownGracefully();
//            defaultExecutor.shutdownGracefully();
        } catch (Exception e) {
            System.err.println("**********NettyServer服务关闭异常！！！**********");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClose() {
        return close;
    }


    @ChannelHandler.Sharable           // 在handler在channels间共享
    public static class EchoServerHandler extends ChannelInboundHandlerAdapter {
        private static Logger log = LoggerFactory.getLogger(EchoServerHandler.class);
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            log.info("Server received: {}", in.toString(CharsetUtil.UTF_8));
            ctx.write(in);     // 将接收到的消息发送回给客户端，这里并没有flush
        }
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // flush所有前面（pending）的消息，在所有操作完成后，关闭远程channel
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            System.out.println("channelReadComplete 已经完成");
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Server Error:", cause);
            ctx.close();    // 发生异常后，关闭远程channel
        }
    }

    public static void main(String[] args) throws Exception {
//        AbstractNettyServer server = new AbstractNettyServer(new InetSocketAddress(8081));
//        server.start();
//        Thread.sleep(20000);
//        server.close();
    }
}
