package com.study.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一响应结果
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.data = object;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = success();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
