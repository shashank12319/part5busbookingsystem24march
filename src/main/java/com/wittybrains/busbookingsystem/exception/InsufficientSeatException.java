package com.wittybrains.busbookingsystem.exception;

public class InsufficientSeatException extends RuntimeException {

    public InsufficientSeatException(String message) {
        super(message);
    }
}

