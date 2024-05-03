package com.neobis.projects.authproject.controllers;


import com.neobis.projects.authproject.dto.UserErrorResponse;
import com.neobis.projects.authproject.dto.UserLoginDTO;
import com.neobis.projects.authproject.dto.UserRegistrationDTO;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.entities.UserStatus;
import com.neobis.projects.authproject.services.RegistrationService;
import com.neobis.projects.authproject.services.UserService;
import com.neobis.projects.authproject.security.JwtTokenUtils;
import com.neobis.projects.authproject.utils.InvalidUsernameOrEmailException;
import com.neobis.projects.authproject.utils.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "Controller for authentication/authorization")
public class AuthController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final UserValidator userValidator;
    private final ModelMapper modelMapper;

    // Endpoint for registration: /api/register
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Endpoint for user registration. Returns jwt token.")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO userRegistrationDTO, BindingResult bindingResult) {
        userValidator.validate(modelMapper.map(userRegistrationDTO, User.class), bindingResult);
        if(bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            StringBuilder errorMessage = new StringBuilder();
            for(FieldError error: fieldErrors) {
                errorMessage.append(error.getDefaultMessage()).append(". ");
            }
            throw new InvalidUsernameOrEmailException(errorMessage.toString());
        }
        registrationService.registerUser(userRegistrationDTO);
        String token = jwtTokenUtils.generateToken(userService.loadUserByUsername(userRegistrationDTO.getUsername()));
        return new ResponseEntity<>(Map.of("accessToken", token), HttpStatus.CREATED);
    }


    // Endpoint for login: /api/login
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Endpoint for user login. Returns jwt token.")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),
                userLoginDTO.getPassword());
        try{
            authenticationManager.authenticate(authentication);
        }catch (BadCredentialsException ex) {
            return new ResponseEntity<>(new UserErrorResponse("Invalid username or password", System.currentTimeMillis()), HttpStatus.BAD_REQUEST);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userService.loadUserByUsername(userLoginDTO.getUsername());
        userService.loginUser(userLoginDTO.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);


        return new ResponseEntity<>(Map.of("accessToken", token), HttpStatus.OK);
    }

    // Endpoint for Home page
    @GetMapping("/me")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Home page", description = "Home page is available after registration.")
    public ResponseEntity<?> homePage(Principal principal) {
        User user = userService.findByUsername(principal.getName()).get();
        if(user.getIsFirstTime().equals(UserStatus.FIRST_TIME)) {
            userService.setUserStatus(user, UserStatus.NOT_FIRST_TIME);
            return new ResponseEntity<>(Map.of("username", user.getUsername(),
                        "userStatus", "First time"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(Map.of("username", user.getUsername(),
                    "userStatus", "Not first time"), HttpStatus.OK);
        }
    }

    // Endpoint for logout
    @GetMapping("/logout")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "User logout", description = "Endpoint for user logout.")
    public ResponseEntity<?> logout(Principal principal) {
        userService.logoutUser(principal.getName());
        return new ResponseEntity<>(Map.of("message", String.format("User %s is logged out", principal.getName())), HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<?> invalidUsernameOrEmailExceptionHandler(InvalidUsernameOrEmailException ex) {
        UserErrorResponse userErrorResponse = new UserErrorResponse(ex.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(userErrorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<?> usernameNotFoundExceptionHandler(UsernameNotFoundException ex) {
        UserErrorResponse userErrorResponse = new UserErrorResponse(ex.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(userErrorResponse, HttpStatus.NOT_FOUND);
    }
}
