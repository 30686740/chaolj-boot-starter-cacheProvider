package com.chaolj.core.cacheProvider;

import cn.hutool.core.util.StrUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.chaolj.core.commonUtils.myDelegate.ActionDelegate;
import com.chaolj.core.commonUtils.myServer.Interface.ICacheServer;

public class MyCacheProvider implements ICacheServer {
    private ApplicationContext applicationContext;
    private MyCacheProviderProperties properties;

    private RedissonClient client;

    private String globalKeyPre;
    private String appKeyPre;

    public MyCacheProvider(ApplicationContext applicationContext, MyCacheProviderProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;

        this.client = (RedissonClient)applicationContext.getBean("redissonClient");

        this.globalKeyPre = "global;#";
        this.appKeyPre = applicationContext.getEnvironment().getProperty("spring.application.name") + ";#";
    }

    public <T> void ListSet(String key, Collection<T> list) {
        this.ListSet(key, list, this.properties.getDefExpireSeconds());
    }

    public <T> void ListSet(String key, Collection<T> list, long seconds) {
        if (StrUtil.isBlank(key)) return;
        if (list == null) return;
        if (list.isEmpty()) return;
        var rList = this.client.<T>getList(key);
        rList.expire(seconds, TimeUnit.SECONDS);
        rList.addAll(list);
    }

    public <T> List<T> ListGet(String key) {
        if (StrUtil.isBlank(key)) return null;
        return this.client.<T>getList(key).readAll();
    }

    public <T> void ListRemove(String key, Collection<T> list) {
        if (StrUtil.isBlank(key)) return;
        if (list == null) return;
        if (list.isEmpty()) return;
        this.client.getList(key).removeAll(list);
    }

    public void ListClear(String key) {
        if (StrUtil.isBlank(key)) return;
        this.client.getList(key).clear();
    }

    public boolean GlobalHasKey(String key) {
        if (StrUtil.isBlank(key)) return false;

        var bucket = client.getBucket(this.globalKeyPre + key);
        return bucket.get() != null;
    }

    public Object GlobalGet(String key) {
        if (StrUtil.isBlank(key)) return null;
        return client.getBucket(this.globalKeyPre + key).get();
    }

    public void GlobalSet(String key, Object value, long seconds) {
        if (StrUtil.isBlank(key)) return;
        client.getBucket(this.globalKeyPre + key).set(value, seconds, TimeUnit.SECONDS);
    }

    public void GlobalSet(String key, Object value) {
        this.GlobalSet(key, value, this.properties.getDefExpireSeconds());
    }

    public void GlobalRemove(String... key) {
        if (key == null) return;
        if (key.length <= 0) return;

        for(var item : key) {
            if (StrUtil.isBlank(item)) continue;
            client.getBucket(this.globalKeyPre + item).delete();
        }
    }

    public boolean AppHasKey(String key) {
        if (StrUtil.isBlank(key)) return false;

        var bucket = client.getBucket(this.appKeyPre + key);
        return bucket.get() != null;
    }

    public Object AppGet(String key) {
        if (StrUtil.isBlank(key)) return null;
        return client.getBucket(this.appKeyPre + key).get();
    }

    public void AppSet(String key, Object value, long seconds) {
        if (StrUtil.isBlank(key)) return;
        client.getBucket(this.appKeyPre + key).set(value, seconds, TimeUnit.SECONDS);
    }

    public void AppSet(String key, Object value) {
        this.AppSet(key, value, this.properties.getDefExpireSeconds());
    }

    public void AppRemove(String... key) {
        if (key == null) return;
        if (key.length <= 0) return;

        for(var item : key) {
            if (StrUtil.isBlank(item)) continue;
            client.getBucket(this.appKeyPre + item).delete();
        }
    }

    public void RunWithLock(String key, ActionDelegate action) {
        if (StrUtil.isBlank(key)) return;
        if (action == null) return;

        var lock = this.client.getLock(key);

        try {
            var res = lock.tryLock(this.properties.getDefWaitLockSeconds(), this.properties.getDefUnLockSeconds(), TimeUnit.SECONDS);
            if (!res) throw new RuntimeException("获取锁对象失败！大于超时时间（" + this.properties.getDefWaitLockSeconds() + "s）仍未获取到锁，请重试！");

            action.invoke();
        }
        catch (InterruptedException ex) {
            throw new RuntimeException("获取锁对象失败！" + ex.getMessage());
        }
        finally {
            lock.unlock();
        }
    }

    public void RunWithLock(List<String> keys, ActionDelegate action) {
        if (keys == null) return;
        if (keys.stream().count() <= 0) return;
        if (action == null) return;

        var locks = new ArrayList<RLock>();
        for (var key : keys) {
            if (StrUtil.isBlank(key)) continue;

            var lock = this.client.getLock(key);
            locks.add(lock);
        }
        if (locks.stream().count() <= 0) return;

        var multLock = this.client.getMultiLock(locks.toArray(new RLock[0]));
        try {
            var res = multLock.tryLock(this.properties.getDefWaitLockSeconds(), this.properties.getDefUnLockSeconds(), TimeUnit.SECONDS);
            if (!res) throw new RuntimeException("获取锁对象失败！大于超时时间（" + this.properties.getDefWaitLockSeconds() + "s）仍未获取到锁，请重试！");

            action.invoke();
        }
        catch (InterruptedException ex) {
            throw new RuntimeException("获取锁对象失败！" + ex.getMessage());
        }
        finally {
            multLock.unlock();
        }
    }
}
