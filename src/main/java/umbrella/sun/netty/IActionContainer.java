package umbrella.sun.netty;

/**
 * Created by paladintyrion on 17/2/10.
 *
 * @author <a href="mailto: paladinosmenttt@sina.com" /> paladintyrion
 * @version 1.0.0
 */
public interface IActionContainer {

    /**
     * 启动服务
     */
    void start() throws Exception;

    /**
     * 初始化配置
     */
    void init();

    /**
     * 关闭服务
     */
    void close();

    /**
     * 是否服务即将关闭，若关闭，则不再接收新任务
     * @return
     */
    boolean isClose();
}
