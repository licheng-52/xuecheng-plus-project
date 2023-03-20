package com.xuecheng.base.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){
        log.info("系统异常{}",e.getErrMessage(),e);
        return new RestErrorResponse(e.getErrMessage());
    }


    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        log.info("系统异常{}",e.getMessage(),e);

        if(e.getMessage().equals("不允许访问")){
            return new RestErrorResponse("没有操作功能权限");
        }
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }


    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        StringBuffer stringBuffer = new StringBuffer();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        fieldErrors.forEach(error->{
            stringBuffer.append(error.getDefaultMessage()).append(",");
        });
        log.error(stringBuffer.toString());
        return new RestErrorResponse(stringBuffer.toString());
    }
}
