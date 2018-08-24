package umbrella.sun.serialize;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by paladintyrion on 17/1/3.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public class KryoCodecUtil implements MessageCodecUtil {

    private KryoPool kryoPool;

    public KryoCodecUtil(KryoPool kryoPool) {
        this.kryoPool = kryoPool;
    }

    @Override
    public void encode(ByteBuf out, Object message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            KryoSerialize kryoSerialize = new KryoSerialize(this.kryoPool);
            kryoSerialize.serialize(byteArrayOutputStream, message);
            byte[] body = byteArrayOutputStream.toByteArray();
            int dataSize = body.length;
            out.writeInt(dataSize);
            out.writeBytes(body);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            byteArrayOutputStream.close();
        }

    }

    @Override
    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(body);
            KryoSerialize kryoSerialize = new KryoSerialize(this.kryoPool);
            Object object = kryoSerialize.deserialize(byteArrayInputStream);
            return object;
        } finally {
            byteArrayInputStream.close();
        }
    }
}
