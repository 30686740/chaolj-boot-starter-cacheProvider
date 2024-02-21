package com.chaolj.core.cacheProvider.AutoConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {
    private String host;
    private int port;
    private String password;
    private int database = 0;
    private int idleConnectionTimeout = 10000;
    private int connectTimeout = 10000;
    private int timeout = 3000;
    private int retryAttempts = 3;
    private int retryInterval = 1500;
    private int connectionPoolSize = 64;
    private int connectionMinimumIdleSize = 24;

    // 集群模式
    private String nodes;
    private int scanInterval = 5000;
    private int slaveConnectionMinimumIdleSize = 24;
    private int slaveConnectionPoolSize = 64;
    private int masterConnectionMinimumIdleSize = 24;
    private int masterConnectionPoolSize = 64;
}
