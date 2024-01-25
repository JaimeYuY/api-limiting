package com.ocean.angel.tool.config;

import com.ocean.angel.tool.interceptor.ApiLimitingInterceptor;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RedissonClient redissonClient;

    public WebConfig(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiLimitingInterceptor(redissonClient))
                .addPathPatterns("/**"); // 设置拦截的路径
    }
}
