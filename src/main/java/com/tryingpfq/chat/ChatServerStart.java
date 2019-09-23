package com.tryingpfq.chat;

import com.tryingpfq.chat.handler.server.HttpHandler;
import com.tryingpfq.chat.handler.server.WebSocktHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServerStart {

    private static Logger LOG = LoggerFactory.getLogger(ChatServerStart.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    private static final int port = 88;

    public static void main(String[] args) {
        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(channelInitializer());
        try {
            ChannelFuture f = b.bind(port).sync();
            LOG.info("server is start port:{}",port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static ChannelInitializer channelInitializer(){
        return new ChannelInitializer<SocketChannel>(){

            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();

                //HTTP
                pipeline.addLast(new HttpServerCodec());
                //主要是将同一个http请求或响应的多个消息对象变成一个 fullHttpRequest完整的消息对象
                pipeline.addLast(new HttpObjectAggregator(64 * 1024));

                //主要用于处理大数据流,比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的 ,加上这个handler我们就不用考虑这个问题了
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new HttpHandler());

                /** 解析WebSocket请求 */
                pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                pipeline.addLast(new WebSocktHandler());
            }
        };
    }
}
