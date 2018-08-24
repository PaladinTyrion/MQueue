package umbrella.sun.serialize;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * Created by paladintyrion on 17/1/3.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public interface MessageCodecUtil {

    public final static int MESSAGE_LENGTH = 4;

    public void encode(final ByteBuf out, final Object message) throws IOException;

    public Object decode(byte[] body) throws IOException;
}
