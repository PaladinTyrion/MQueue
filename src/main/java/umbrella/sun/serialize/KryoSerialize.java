package umbrella.sun.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by paladintyrion on 17/1/3.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public class KryoSerialize {
    private KryoPool kryoPool = null;
    private Closer closer = Closer.create();

    public KryoSerialize(final KryoPool kryoPool) {
        this.kryoPool = kryoPool;
    }

    public void serialize(OutputStream outputStream, Object obj) throws IOException {
        try {
            Kryo kryo = kryoPool.borrow();
            Output output = new Output(outputStream);
            kryo.writeClassAndObject(output, obj);
            closer.register(outputStream);
            closer.register(output);
            kryoPool.release(kryo);
        } finally {
            closer.close();
        }
    }

    public Object deserialize(InputStream inputStream) throws IOException {
        try {
            Kryo kryo = kryoPool.borrow();
            Input input = new Input(inputStream);
            Object ret = kryo.readClassAndObject(input);
            closer.register(inputStream);
            closer.register(input);
            kryoPool.release(kryo);
            return ret;
        } finally {
            closer.close();
        }
    }
}
