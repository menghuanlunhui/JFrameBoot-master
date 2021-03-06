package com.jf.system.redisson.lock;

/**
 * Created with IntelliJ IDEA.
 * Description:获取锁后需要处理的逻辑
 * User: admin
 * Date: 2018-01-03
 * Time: 09:52
 */
public interface AquiredLockWorker<T> {

    T invokeAfterLockAquire() throws Exception;

}
