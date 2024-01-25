package com.ocean.angel.tool.constant;

/**
 *  API 限流类型枚举类
 */
public enum ApiLimitingTypeEnum {

    API_REQUEST_LIMIT(1, "API请求限流数"),
    API_IP_LIMIT(2, "API IP限流数");

    private int code;

    private String msg;

    ApiLimitingTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
