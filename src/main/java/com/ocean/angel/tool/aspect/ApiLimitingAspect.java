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
