package com.sczyh30.todolist.entity;

/**
 * Process Status Entity
 */
public class ProcessStatus {

    private int code;
    private String msg;

    public ProcessStatus() {}

    public ProcessStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ProcessStatus: {" +
                "code: " + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
