package com.ocean.angel.tool.exception;


public class GlobalException extends Exception {

    private static final long serialVersionUID = - 5583965576801968391L;

    // 返回错误码
    private Integer code;

    public GlobalException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public GlobalException(Integer code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public GlobalException (Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    protected GlobalException(Integer code, String msg, Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(msg, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
