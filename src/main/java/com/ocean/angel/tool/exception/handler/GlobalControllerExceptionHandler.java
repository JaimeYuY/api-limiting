package com.ocean.angel.tool.exception.handler;

import com.ocean.angel.tool.common.ResultBean;
import com.ocean.angel.tool.exception.BusinessException;
import com.ocean.angel.tool.exception.ErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.SystemException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 系统全局异常捕捉
 */
@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {


    @ExceptionHandler(ErrorCodeException.class)
    @ResponseBody
    public ResultBean handleErrorCodeException(ErrorCodeException e) {

        Integer code = e.getErrorCode();
        String msg = e.getMessage();

        // 外部传入了消息描述，这个地方就不再覆盖了
        if (!StringUtils.hasLength(msg)) {
            msg = "服务器正忙，请稍后重试。";
        }

        if (e instanceof BusinessException) {
            log.info("Business exception : msg {} : code {}", msg, code);
        } else {
            log.error("ErrorCodeException exception : msg {}", msg, e);
        }

        return ResultBean.error(code, msg);
    }


    /**
     * 空指针
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResultBean nullPointerException(NullPointerException e) {
        log.error("发生空指针异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 类型强制转换异常
     */
    @ExceptionHandler(value = ClassCastException.class)
    @ResponseBody
    public ResultBean classCastException(ClassCastException e) {
        log.error("类型强制转换异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 数组下标越界异常
     */
    @ExceptionHandler(value = ArrayIndexOutOfBoundsException.class)
    @ResponseBody
    public ResultBean arrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        log.error("数组下标越界异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 字符串转换为数字异常
     */
    @ExceptionHandler(value = NumberFormatException.class)
    @ResponseBody
    public ResultBean numberFormatException(NumberFormatException e) {
        log.error("字符串转换为数字异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 输入输出异常
     */
    @ExceptionHandler(value = IOException.class)
    @ResponseBody
    public ResultBean IOException(IOException e) {
        log.error("输入输出异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 方法未找到异常
     */
    @ExceptionHandler(value = NoSuchMethodException.class)
    @ResponseBody
    public ResultBean noSuchMethodException(NoSuchMethodException e) {
        log.error("方法未找到异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 系统异常
     */
    @ExceptionHandler(value = SystemException.class)
    @ResponseBody
    public ResultBean systemException(SystemException e) {
        log.error("系统异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 非法参数异常
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public ResultBean IllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常！原因是:", e);
        return ResultBean.error(HttpStatus.BAD_REQUEST.value(), e.toString());
    }


    /**
     * 不支持的操作异常
     */
    @ExceptionHandler(value = UnsupportedOperationException.class)
    @ResponseBody
    public ResultBean unsupportedOperationException(UnsupportedOperationException e) {
        log.error("不支持的操作异常！原因是:", e);
        return ResultBean.error(HttpStatus.BAD_REQUEST.value(), e.toString());
    }


    /**
     * 操作数据库异常
     */
    @ExceptionHandler(value = SQLException.class)
    @ResponseBody
    public ResultBean sqlException(SQLException e) {
        log.error("操作数据库异常！原因是:", e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 参数校验失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResultBean handlerConstraintViolationException(BindException e) {
        log.warn(e.getMessage(), e);

        if (!e.getAllErrors().isEmpty()) {
            String msg = e.getAllErrors().get(0).getDefaultMessage();
            return ResultBean.error(HttpStatus.BAD_REQUEST.value(), msg);
        } else {
            return ResultBean.error(HttpStatus.BAD_REQUEST.value(), e.toString());
        }

    }


    /**
     * 方法参数无效异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultBean handlerArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn(e.getMessage(), e);

        if (!e.getBindingResult().getAllErrors().isEmpty()) {
            String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
            return ResultBean.error(HttpStatus.BAD_REQUEST.value(), msg);

        } else {
            return ResultBean.error(HttpStatus.BAD_REQUEST.value(), e.toString());
        }
    }


    /**
     * Internal Server Error 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResultBean handlerException(Exception e) {
        log.error(e.getMessage(), e);
        return ResultBean.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
    }


    /**
     * 404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResultBean noHandlerFoundException(NoHandlerFoundException e) {
        return ResultBean.error(HttpStatus.NOT_FOUND.value(),  e.toString());
    }
}
