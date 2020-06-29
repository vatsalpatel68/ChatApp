package com.example.chatapp;

public class getChatClass {

    public String data;
    public String status;

    public getChatClass() {
    }

    public getChatClass(String data, String status) {
        this.data = data;
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
