package org.example.app.service;

public class RestoreCodeNotFoundException extends RuntimeException {
    public RestoreCodeNotFoundException() {
    }

    public RestoreCodeNotFoundException(String message) {
        super(message);
    }

    public RestoreCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestoreCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public RestoreCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
