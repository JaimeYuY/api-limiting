# springboot项目接口限流方案
## 系统限流要求
1. 每秒系统总并发数，如设置1000，表示该系统接口每秒可以请求1000次
2. 自定义系统接口请求并发数，也可以不加限流设置，如设置100，表示每秒可以请求100次该接口
3. 指定接口IP请求并发数，如设置1，表示每秒该IP可以请求1次该接口

## 实现思路
1. 每秒系统总并发数限流实现，可以使用拦截器或过滤器，来处理系统总并发数限流的实现
2. 自定义系统接口请求并发数和指定接口IP请求并发数的实现，可以使用自定义注解和切面，来处理自定义系统接口请求并发数的实现
3. 可以使用Redisson RRateLimiter组件实现具体限流逻辑
4. 自定义业务异常类，再请求数超出请求限制时，打断业务

## 核心代码
1. 接口限流注解
```
package com.ocean.angel.tool.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiLimiting {

    // 接口请求限制数
    int apiRequestLimit() default 200;

    // 接口请求IP限制数
    int apiIpLimit() default 1;
}

```
2. 接口限流切面
```
package com.ocean.angel.tool.aspect;

import com.ocean.angel.tool.annotation.ApiLimiting;
import com.ocean.angel.tool.constant.ApiLimitingTypeEnum;
import com.ocean.angel.tool.constant.ResultCode;
import com.ocean.angel.tool.dto.ApiLimitingData;
import com.ocean.angel.tool.exception.BusinessException;
import com.ocean.angel.tool.util.RateLimiterKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 接口限流切面
 */
@Slf4j
@Aspect
@Component
public class ApiLimitingAspect {

    @Resource
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.ocean.angel.tool.annotation.ApiLimiting)")
    public void apiLimitingAspect() {}

    @Before(value = "apiLimitingAspect()")
    public void apiLimiting(JoinPoint joinPoint) {
        ApiLimitingData apiLimitingData = getApiLimitData(joinPoint);
        rateLimiterHandler(redissonClient, apiLimitingData);
    }

    /**
     * API 限流逻辑处理
     */
    private void rateLimiterHandler(RedissonClient redissonClient, ApiLimitingData apiLimitingData) {

        if(apiLimitingData.getApiIpLimit() > 0) {

            // 获取RRateLimiter实例
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(getRateLimiterKey(apiLimitingData, ApiLimitingTypeEnum.API_IP_LIMIT));

            // RRateLimiter初始化
            if(!RateLimiterKeyUtil.contains(getRateLimiterKey(apiLimitingData, ApiLimitingTypeEnum.API_IP_LIMIT))) {
                rateLimiter.trySetRate(RateType.OVERALL, apiLimitingData.getApiIpLimit(), 1, RateIntervalUnit.SECONDS);
            }

            // 超出接口请求IP限流设置，打断业务
            if (!rateLimiter.tryAcquire()) {
                log.info("接口{}超出IP请求限制, 时间：{}",apiLimitingData.getMethodName(), System.currentTimeMillis());
                throw new BusinessException(ResultCode.BEYOND_RATE_LIMIT);
            }
        }

        if(apiLimitingData.getApiRequestLimit() > 0) {

            RRateLimiter rateLimiter = redissonClient.getRateLimiter(getRateLimiterKey(apiLimitingData, ApiLimitingTypeEnum.API_REQUEST_LIMIT));

            if(!RateLimiterKeyUtil.contains(getRateLimiterKey(apiLimitingData, ApiLimitingTypeEnum.API_REQUEST_LIMIT))) {
                rateLimiter.trySetRate(RateType.OVERALL, apiLimitingData.getApiRequestLimit(), 1, RateIntervalUnit.SECONDS);
            }

            // 超出接口请求限流设置，打断业务
            if (!rateLimiter.tryAcquire()) {
                log.info("接口{}超出请求限制, 时间：{}",apiLimitingData.getMethodName(), System.currentTimeMillis());
                throw new BusinessException(ResultCode.BEYOND_RATE_LIMIT);
            }
        }
    }

    /**
     * 组装ApiLimitingData
     */
    private ApiLimitingData getApiLimitData(JoinPoint joinPoint) {

        ApiLimitingData apiLimitingData = new ApiLimitingData();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        apiLimitingData.setMethodName(method.getName());

        ApiLimiting apiLimiting = method.getAnnotation(ApiLimiting.class);
        apiLimitingData.setApiRequestLimit(apiLimiting.apiRequestLimit());
        apiLimitingData.setApiIpLimit(apiLimiting.apiIpLimit());

        return apiLimitingData;
    }

    /**
     * RateLimiter Key
     */
    private String getRateLimiterKey(ApiLimitingData apiLimitingData, ApiLimitingTypeEnum apiLimitingTypeEnum) {
        return apiLimitingData.getMethodName() + "_" + apiLimitingTypeEnum.getCode();
    }
}

```
3. 系统接口限流拦截器
```
package com.ocean.angel.tool.interceptor;

import com.ocean.angel.tool.constant.ResultCode;
import com.ocean.angel.tool.exception.BusinessException;
import com.ocean.angel.tool.util.RateLimiterKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ApiLimitingInterceptor implements HandlerInterceptor {

    private final static String API_TOTAL_LIMIT = "apiTotalLimit";

    // 系统每秒请求总数，30表示每秒最多处理30个请求
    private final static int API_TOTAL_LIMIT_NUMBER = 30;
    private final RedissonClient redissonClient;

    public ApiLimitingInterceptor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(API_TOTAL_LIMIT);

        if(!RateLimiterKeyUtil.contains(API_TOTAL_LIMIT)) {
            rateLimiter.trySetRate(RateType.OVERALL, API_TOTAL_LIMIT_NUMBER, 1, RateIntervalUnit.SECONDS);
        }

        // 超出系统接口总请求数限制，打断业务
        if (!rateLimiter.tryAcquire()) {
            log.info("超出系统接口总请求数限制, 时间：{}", System.currentTimeMillis());
            throw new BusinessException(ResultCode.BEYOND_RATE_LIMIT);
        }
        return true;
    }
}

```
4. 接口自定义注解配置
```
@ApiLimiting(apiRequestLimit = 10, apiIpLimit = 3)
@GetMapping("/limited/resource")
public ResultBean<?> limitedResource() {
    return ResultBean.success();
}
```

## 限流方案演示
1. 下载源代码
2. 修改application.yml和redission.yml，关于redis的相关配置
3. 启动项目，调用http://localhost:8090/test/limited/resource接口，截图如下：
4. 保持项目启动状态，运行com.ocean.angel.tool.ApplicationTests.contextLoads()方法，截图如下


## 使用指南
1. 修改系统总请求数限制
2. 调整系统接口限流参数
3. 本文使用Redisson RRateLimiter组件实现具体限流逻辑，小伙伴们可以自己去手写具体限流功能（可以参考Redission的限流相关的数据结构）
