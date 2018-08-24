package umbrella.sun.serialize;

/**
 * Created by paladintyrion on 17/2/28.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public interface IByteSerializer<T> {

    /**
     * 序列化
     * @param obj
     * @return
     * @throws Exception
     */
    byte[] serialize(T obj) throws Exception;

    /**
     * 反序列化
     * @param bs
     * @return
     * @throws Exception
     */
    T deserialize(byte[] bs, Class<T> cls) throws Exception;
}
