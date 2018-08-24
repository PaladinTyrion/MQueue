package umbrella.sun.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Created by paladintyrion on 17/2/28.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
@Slf4j
public class RpcResponse implements Serializable {
    @Getter
    @Setter
    private String requestId;

    @Getter
    @Setter
    private Throwable errMsg;

    @Getter
    @Setter
    private Boolean error;

    @Getter
    @Setter
    private Object result;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RpcResponse that = (RpcResponse) obj;
        if (getRequestId() == null || error == null) {
            log.error("This RpcResponse is broken and Method of \"equals\" returns false directly!");
            return false;
        }
        if (that.getRequestId() == null || that.error == null) {
            log.error("Anthor RpcResponse is broken and Method of \"equals\" returns false directly!");
            return false;
        }

        if (!getRequestId().equals(that.getRequestId())) return false;
        if (!getError().equals(that.getError())) return false;
        if (getErrMsg() != null ? !getErrMsg().equals(that.getErrMsg()) : that.getErrMsg() != null) return false;
        return getResult() != null ? getResult().equals(that.getResult()) : that.getResult() == null;

    }
}
