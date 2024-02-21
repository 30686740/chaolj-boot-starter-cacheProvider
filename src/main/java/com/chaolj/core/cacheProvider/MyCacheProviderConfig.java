package com.chaolj.core.cacheProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.chaolj.core.commonUtils.myServer.Interface.ICacheServer;

@Configuration
@EnableConfigurationProperties(MyCacheProviderProperties.class)
public class MyCacheProviderConfig {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MyCacheProviderProperties myCacheProviderProperties;

    @Bean(name = "myCacheProvider")
    public ICacheServer MyCacheProvider(){
        return new MyCacheProvider(this.applicationContext, this.myCacheProviderProperties);
    }
}
