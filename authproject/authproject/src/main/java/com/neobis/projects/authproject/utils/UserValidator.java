package com.neobis.projects.authproject.utils;

import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {
    private final UserService userService;
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        if(userService.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "This username has already taken");
        }
        if(userService.findByEmail(user.getEmail()).isPresent()) {
            errors.rejectValue("email", "", "This email has already taken");
        }
    }
}
