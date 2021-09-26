package org.example.app.exception;

public class WrongAccessException extends RuntimeException{
    public WrongAccessException() {
    }

    public WrongAccessException(String message) {
        super(message);
    }

    public WrongAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongAccessException(Throwable cause) {
        super(cause);
    }

    public WrongAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
