package umbrella.sun.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Created by paladintyrion on 17/1/3.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public class KryoPoolFactory {

    private static KryoPoolFactory kryoPoolFactory = new KryoPoolFactory();

    private KryoPoolFactory() {}

    private KryoFactory kryoFactory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            //Set all conf
            kryo.setReferences(false);
            /*kryo.register(Request.class);
            kryo.register(Response.class);*/
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    private KryoPool kryoPool = new KryoPool.Builder(kryoFactory).build();

    public static KryoPool getKryoPoolInstance() {
        return kryoPoolFactory.getKryoPool();
    }

    public KryoPool getKryoPool() {
        return kryoPool;
    }
}
