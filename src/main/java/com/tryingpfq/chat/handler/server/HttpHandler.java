package com.tryingpfq.chat.handler.server;

import com.google.common.base.Preconditions;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static Logger LOG = Logger.getLogger(HttpHandler.class);
	
	//获取class路径
    private URL baseURL = HttpHandler.class.getProtectionDomain().getCodeSource().getLocation();
    private final String webroot = "webroot";

    private File getResoure(String fileName) throws URISyntaxException {
        String path = null;
            //URL url = HttpHandler.class.getClassLoader().getResource();
        path = baseURL.toURI() + webroot + "/" + fileName;

        path = !path.contains("file:") ? path : path.substring(5);
        path = path.replaceAll("//","/");
        File file = new File(path);
        Preconditions.checkNotNull(file, "file is null fileName:[%s]", fileName);
        return file;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.getUri();
        
        RandomAccessFile file = null;

        try{
            String page = uri.equals("/") ? "chat.html" : uri;
            file =	new RandomAccessFile(getResoure(page), "r");
        }catch(Exception e){
            ctx.fireChannelRead(request.retain());
            return;
        }

        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
        String contextType = "text/html;";
        if(uri.endsWith(".css")){
        		contextType = "text/css;";
        }else if(uri.endsWith(".js")){
        		contextType = "text/javascript;";
        }else if(uri.toLowerCase().matches("(jpg|png|gif)$")){
        		String ext = uri.substring(uri.lastIndexOf("."));
        		contextType = "image/" + ext;
        }
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contextType + "charset=utf-8;");

        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);
        
        ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
//        ctx.write(new ChunkedNioFile(file.getChannel()));
        
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        file.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel client = ctx.channel();
        LOG.info("Client:"+client.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}

