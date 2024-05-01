package com.neobis.projects.authproject.utils;

public class InvalidUsernameOrEmailException extends RuntimeException{
    public InvalidUsernameOrEmailException(String message) {
        super(message);
    }
}
