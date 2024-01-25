package com.ocean.angel.tool.exception;

import com.ocean.angel.tool.constant.ResultCode;

/**
 * 业务异常类，只用于业务打断。
 */
public class BusinessException extends ErrorCodeException {

    private static final long serialVersionUID = -45155154531465551L;

    public BusinessException(Integer businessErrorCode) {
        super(businessErrorCode);
    }

    public BusinessException(Integer businessErrorCode, String message, Throwable cause) {
        super(businessErrorCode, message, cause);
    }

    public BusinessException(Integer businessErrorCode, String message) {
        super(businessErrorCode, message);
    }

    public BusinessException(Integer businessErrorCode, Throwable cause) {
        super(businessErrorCode, cause);
    }

    public BusinessException(Integer businessErrorCode, String message, Object result) {
        super(businessErrorCode, message, result);
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg());
    }

    public BusinessException(ResultCode resultCode, Throwable cause) {
        this(resultCode.getCode(), resultCode.getMsg(), cause);
    }

    public static void throwException(ResultCode resultCode) {
        throw new BusinessException(resultCode);
    }

    public static void throwException(ResultCode resultCode,Throwable cause) {
        throw new BusinessException(resultCode, cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
