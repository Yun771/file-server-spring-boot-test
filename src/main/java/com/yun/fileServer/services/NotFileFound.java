package com.yun.fileServer.services;

public class NotFileFound extends Exception {
    public NotFileFound() {

    }

    public NotFileFound(String message) {
        super(message);
    }
}
