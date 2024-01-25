package com.ocean.angel.tool.dto;

import lombok.Data;

@Data
public class ApiLimitingData {

    // 方法名称
    private String methodName;

    // 接口请求限流数
    private int apiRequestLimit;

    // 接口IP限流数
    private int apiIpLimit;
}
