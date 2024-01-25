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
