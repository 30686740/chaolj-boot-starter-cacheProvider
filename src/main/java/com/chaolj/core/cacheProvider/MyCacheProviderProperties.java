package com.chaolj.core.cacheProvider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "myproviders.mycacheprovider")
public class MyCacheProviderProperties {
    private long defExpireSeconds = 3600;  // 默认缓存1个小时
    private long defWaitLockSeconds = 10;  // 获取锁时默认等待超时10秒
    private long defUnLockSeconds = 600;   // 获取锁后默认释放超时600秒
}
