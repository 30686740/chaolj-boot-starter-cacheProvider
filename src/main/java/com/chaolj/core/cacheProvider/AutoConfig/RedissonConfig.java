package com.chaolj.core.cacheProvider.AutoConfig;

import cn.hutool.core.util.StrUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Redisson.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {
    @Autowired
    RedissonProperties redissonProperties;

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        if (StrUtil.isNotBlank(redissonProperties.getHost())) {
            // 单节点配置
            var config = new Config();
            var serverConfig = config.useSingleServer()
                    .setAddress("redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort())
                    .setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
                    .setConnectTimeout(redissonProperties.getConnectTimeout())
                    .setTimeout(redissonProperties.getTimeout())
                    .setRetryAttempts(redissonProperties.getRetryAttempts())
                    .setRetryInterval(redissonProperties.getRetryInterval())
                    .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
                    .setDatabase(redissonProperties.getDatabase())
                    .setKeepAlive(true);

            if (StrUtil.isNotBlank(redissonProperties.getPassword())) {
                serverConfig.setPassword(redissonProperties.getPassword());
            }

            config.setCodec(new JsonJacksonCodec());
            return Redisson.create(config);
        } else {
            // 集群模式
            var nodesList = redissonProperties.getNodes().split(",");
            for (int i = 0; i < nodesList.length; i++) {
                nodesList[i] = "redis://" + nodesList[i];
            }
            var config = new Config();
            var serverConfig = config.useClusterServers()
                    .addNodeAddress(nodesList)
                    .setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
                    .setConnectTimeout(redissonProperties.getConnectTimeout())
                    .setTimeout(redissonProperties.getTimeout())
                    .setRetryAttempts(redissonProperties.getRetryAttempts())
                    .setRetryInterval(redissonProperties.getRetryInterval())
                    .setScanInterval(redissonProperties.getScanInterval())
                    .setSlaveConnectionMinimumIdleSize(redissonProperties.getSlaveConnectionMinimumIdleSize())
                    .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(redissonProperties.getMasterConnectionMinimumIdleSize())
                    .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                    .setKeepAlive(true);

            if (!StrUtil.isEmpty(redissonProperties.getPassword())) {
                serverConfig.setPassword(redissonProperties.getPassword());
            }
            return Redisson.create(config);
        }
    }
}
