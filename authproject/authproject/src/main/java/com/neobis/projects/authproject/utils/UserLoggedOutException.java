package com.neobis.projects.authproject.utils;

public class UserLoggedOutException extends RuntimeException{
    public UserLoggedOutException(String message) {
        super(message);
    }
}
