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

    private final static String API_TOTAL_LIMIT_KEY = "ApiTotalRateLimiter";

    // 系统每秒请求总数，30表示每秒最多处理30个请求
    private final static int API_TOTAL_LIMIT_NUMBER = 10;
    private final RedissonClient redissonClient;

    public ApiLimitingInterceptor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(API_TOTAL_LIMIT_KEY);

        if(!RateLimiterKeyUtil.contains(API_TOTAL_LIMIT_KEY)) {
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
