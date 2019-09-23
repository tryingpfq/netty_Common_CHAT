package com.tryingpfq.chat.handler.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @Author Tryingpfq
 * @Time 2019/9/23 23:29
 */
public class WebSocktHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        System.out.println(msg);
    }
}
