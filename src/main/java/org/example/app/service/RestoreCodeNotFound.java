package org.example.app.service;

public class RestoreCodeNotFound extends RuntimeException {
    public RestoreCodeNotFound() {
    }

    public RestoreCodeNotFound(String message) {
        super(message);
    }

    public RestoreCodeNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public RestoreCodeNotFound(Throwable cause) {
        super(cause);
    }

    public RestoreCodeNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
