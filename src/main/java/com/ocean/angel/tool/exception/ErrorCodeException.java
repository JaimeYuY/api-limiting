package com.ocean.angel.tool.exception;

/**
 * 含错误码的异常类
*/
public class ErrorCodeException extends RuntimeException {

	private static final long serialVersionUID = -21116129021561225L;

	// 错误码
	private Integer errorCode;

	// 异常结果返回
	private Object result;

	public ErrorCodeException(Integer errorCode) {
		this(errorCode, null, null);
	}

	public ErrorCodeException(Integer errorCode, String message) {
		this(errorCode, message, null);
	}

	public ErrorCodeException(Integer errorCode, Throwable cause) {
		this(errorCode, null, cause);
	}

	public ErrorCodeException(Integer errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ErrorCodeException(Integer errorCode, String message,Object result) {
		super(message);
		this.errorCode = errorCode;
		this.result = result;
	}


	public Integer getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	public Object getResult(){
		return this.result;
	}
}
