package umbrella.sun.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * Created by paladintyrion on 17/2/10.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public class AbstractNettyClient implements INettyClient {

    /* 远程Netty服务地址 */
    private SocketAddress remoteAddr;

    /* 引导服务引导 */
    private Bootstrap bootstrap;

    /* 消息异步处理线程池 */
    private EventLoopGroup eventLoopGroup;

    /* 监听事件列表 */
    private List<ChannelHandler> handlerList;

    /* 是否连接成功 */
    private volatile boolean connected;

    /* 是否关闭 */
    private volatile boolean close;

    /* 连接成功后的channel结果对象 */
    private ChannelFuture channelFuture;

    /* 构造函数 */
    public AbstractNettyClient(SocketAddress remoteAddr, List<ChannelHandler> handlerList) {
        this.remoteAddr = remoteAddr;
        this.handlerList = handlerList;
    }

    /* 构造函数 */
    public AbstractNettyClient(SocketAddress remoteAddr) {
        this(remoteAddr, null);
    }

    @Override
    public void start() throws Exception {
        try {
            if (bootstrap == null) {
                System.out.println("**********启动NettyClient处理进程！！！**********");
                init();
            }

            channelFuture = bootstrap.connect(this.remoteAddr).sync();
            System.out.println("服务地址：" + this.remoteAddr.toString() + " 已连接！！！");
            connected = true;
            close = false;

            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

    @Override
    public void init() {
        try {
            eventLoopGroup = new NioEventLoopGroup();

            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                //不使用nagle算法，有消息就发
                .option(ChannelOption.TCP_NODELAY, true)
                //不启用心跳保活
                .option(ChannelOption.SO_KEEPALIVE, false)
                //send缓冲区buf的大小
                .option(ChannelOption.SO_SNDBUF, 65535)
                //recv缓冲区buf的大小
                .option(ChannelOption.SO_RCVBUF, 65535)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO))
                            .addLast(new EchoClientHandler());
                        //TODO: 此处应有序列化监听的处理
                    }
                });

        } catch (Exception e) {
            System.out.println("**********初始化NettyClient异常！！！**********");
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (isClose()) {
            return;
        }
        if (isConnected()) {
            connected = false;
            try {
                channelFuture.channel().close().sync();
                System.out.println("*****has closed.*****");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        close = true;
        try {
            eventLoopGroup.shutdownGracefully();
        } catch (Exception e) {
            System.out.println("**********NettyClient关闭异常！！！**********");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClose() {
        return close;
    }

    public boolean isConnected() {
        return connected;
    }

    @ChannelHandler.Sharable
    public static class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private static Logger log = LoggerFactory.getLogger(EchoClientHandler.class);
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 当该客户端连接建立并连接到server后，发送消息到Server
            ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, Netty", CharsetUtil.UTF_8));
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();   // 当异常发生时，打印堆栈信息并关闭连接
        }
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            // 接收并打印来自服务端的消息
            log.info("Client Read: {}", ByteBufUtil.hexDump(msg.readBytes(msg.readableBytes())));
        }
    }

    public static void main(String[] args) throws Exception {
        AbstractNettyClient client = new AbstractNettyClient(new InetSocketAddress("10.236.26.189", 8081));
        client.start();
//        Thread.sleep(4000);
//        client.close();
    }
}
