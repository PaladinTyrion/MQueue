package umbrella.sun.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by paladintyrion on 17/2/28.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
@Slf4j
public class RpcRequest implements Serializable{

    @Setter
    @Getter
    private String requestId;

    @Setter
    @Getter
    private String className;

    @Setter
    @Getter
    private String methodName;

    @Setter
    @Getter
    private Class<?>[] parameterTypes;

    @Setter
    @Getter
    private Object[] parameters;

    //requestId、className、methodName 不能为null
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RpcRequest that = (RpcRequest) obj;
        if (getRequestId() == null || getClassName() == null || getMethodName() == null) {
            log.error("This RpcRequest is broken and Method of \"equals\" returns false directly!");
            return false;
        }
        if (that.getRequestId() == null || that.getClassName() == null || that.getMethodName() == null) {
            log.error("Another RpcRequest is broken and Method of \"equals\" returns false directly!");
            return false;
        }

        if (!getRequestId().equals(that.getRequestId())) return false;
        if (!getClassName().equals(that.getClassName())) return false;
        if (!getMethodName().equals(that.getMethodName())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getParameterTypes(), that.getParameterTypes())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getParameters(), that.getParameters());
    }
}
