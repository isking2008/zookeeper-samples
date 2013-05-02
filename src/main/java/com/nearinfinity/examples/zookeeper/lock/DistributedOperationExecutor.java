package com.nearinfinity.examples.zookeeper.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

public class DistributedOperationExecutor {

    private ZooKeeper zk;

    public DistributedOperationExecutor(ZooKeeper zk) {
        this.zk = zk;
    }

    public static List<ACL> DEFAULT_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    public Object withLock(String name, String lockPath, DistributedOperation op)
            throws InterruptedException, KeeperException {
        return withLockInternal(name, lockPath, DEFAULT_ACL, op);
    }

    public DistributedOperationResult withLock(String name, String lockPath, DistributedOperation op,
                                               long timeout, TimeUnit unit)
            throws InterruptedException, KeeperException {
        return withLockInternal(name, lockPath, DEFAULT_ACL, op, timeout, unit);
    }

    public Object withLock(String name, String lockPath, List<ACL> acl, DistributedOperation op)
            throws InterruptedException, KeeperException {
        return withLockInternal(name, lockPath, acl, op);
    }

    public DistributedOperationResult withLock(String name, String lockPath, List<ACL> acl, DistributedOperation op,
                                               long timeout, TimeUnit unit)
            throws InterruptedException, KeeperException {
        return withLockInternal(name, lockPath, acl, op, timeout, unit);
    }

    private Object withLockInternal(String name, String lockPath, List<ACL> acl, DistributedOperation op)
            throws InterruptedException, KeeperException {
        BlockingWriteLock lock = new BlockingWriteLock(name, zk, lockPath, acl);
        try {
            lock.lock();
            return op.execute();
        } finally {
            lock.unlock();
        }
    }

    private DistributedOperationResult withLockInternal(String name, String lockPath, List<ACL> acl,
                                                        DistributedOperation op, long timeout, TimeUnit unit)
    throws InterruptedException, KeeperException {
        BlockingWriteLock lock = new BlockingWriteLock(name, zk, lockPath, acl);
        try {
            boolean lockObtained = lock.lock(timeout, unit);
            if (lockObtained) {
                return new DistributedOperationResult(false, op.execute());
            }
            return new DistributedOperationResult(true, null);
        } finally {
            lock.unlock();
        }
    }

}
