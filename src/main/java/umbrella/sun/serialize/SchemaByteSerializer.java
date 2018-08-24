package umbrella.sun.serialize;

import io.protostuff.Schema;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Created by paladintyrion on 17/3/1.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
@Slf4j
public class SchemaByteSerializer extends SchemaHelper implements IByteSerializer {

    private static Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public byte[] serialize(Object obj) throws Exception {

    }

    @Override
    public Object deserialize(byte[] bs, Class cls) throws Exception {
        Schema schema = getSchema(cls);
        if (schema == null) {
            throw new NullPointerException("Deserializer fails owing to " + cls.getName() + " get null.");
            return null;
        }

    }


}
