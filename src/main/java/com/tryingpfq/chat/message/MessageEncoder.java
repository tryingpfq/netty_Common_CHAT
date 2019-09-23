package com.tryingpfq.chat.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 自定义IM协议的编码器
 */
public class MessageEncoder extends MessageToByteEncoder<IMMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out)
			throws Exception {
		out.writeBytes(new MessagePack().write(msg));
	}
	
	public String encode(IMMessage msg){
		if(null == msg){ return ""; }
		String prex = "[" + msg.getCmd() + "]" + "[" + msg.getTime() + "]";
		if(MessageType.LOGIN.getName().equals(msg.getCmd()) ||
				MessageType.CHAT.getName().equals(msg.getCmd()) ||
				MessageType.FLOWER.getName().equals(msg.getCmd())){
			prex += ("[" + msg.getSender() + "]");
		}else if(MessageType.SYSTEM.getName().equals(msg.getCmd())){
			prex += ("[" + msg.getOnline() + "]");
		}
		if(!(null == msg.getContent() || "".equals(msg.getContent()))){
			prex += (" - " + msg.getContent());
		}
		return prex;
	}

}
