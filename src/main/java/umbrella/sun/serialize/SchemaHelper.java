package umbrella.sun.serialize;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by paladintyrion on 17/3/1.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
@Slf4j
public class SchemaHelper {

    private static LoadingCache<Class<?>, Schema<?>> schemaCache = null;

    private static LoadingCache<Class<?>, Schema<?>> createCache() {
        schemaCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .concurrencyLevel(100)
            .expireAfterWrite(120, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, Schema<?>>() {
                @Override
                public Schema<?> load(Class<?> cls) throws Exception {
                    return RuntimeSchema.createFrom(cls);
                }
            });
        return schemaCache;
    }

    public static <T> Schema<T> getSchema(Class<T> cls) {
        if (schemaCache == null) {
            createCache();
        }
        Schema<T> schema = null;
        try {
            schema = (Schema<T>) schemaCache.get(cls);
            if (schema != null) {
                schemaCache.put(cls, schema);
            }
        } catch (ExecutionException e) {
            log.error("Getting schema fails.", e);
        }
        return schema;
    }
}
