package umbrella.sun.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import umbrella.sun.serialize.IByteSerializer;

import java.util.List;

/**
 * Created by paladintyrion on 17/2/23.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    private IByteSerializer byteSerializer;

    public RpcDecoder(Class<?> genericClass, IByteSerializer byteSerializer) {
        this.genericClass = genericClass;
        this.byteSerializer = byteSerializer;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int byteLen = in.readInt();
        if (byteLen < 0) {
            ctx.close();
            return;
        }

        if (in.readableBytes() < byteLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] reader = new byte[byteLen];
        in.readBytes(reader);

        Object obj = byteSerializer.deserialize(reader, genericClass);
        out.add(obj);
    }
}
