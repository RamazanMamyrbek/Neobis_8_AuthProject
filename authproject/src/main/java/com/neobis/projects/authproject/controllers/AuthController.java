package com.neobis.projects.authproject.controllers;


import com.neobis.projects.authproject.dto.EmailResendDTO;
import com.neobis.projects.authproject.dto.UserErrorResponse;
import com.neobis.projects.authproject.dto.UserLoginDTO;
import com.neobis.projects.authproject.dto.UserRegistrationDTO;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.entities.UserStatus;
import com.neobis.projects.authproject.services.RegistrationService;
import com.neobis.projects.authproject.services.UserService;
import com.neobis.projects.authproject.security.JwtTokenUtils;
import com.neobis.projects.authproject.utils.CustomException;
import com.neobis.projects.authproject.utils.InvalidUsernameOrEmailException;
import com.neobis.projects.authproject.utils.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
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
@Slf4j
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
        log.info("userRegDto: " + userRegistrationDTO.toString());
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
        String token;
        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),
                    userLoginDTO.getPassword());
            authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userService.loadUserByUsername(userLoginDTO.getUsername());
            if(!userDetails.isEnabled())
                throw new CustomException("Email should be confirmed first");
            userService.loginUser(userLoginDTO.getUsername());
            token = jwtTokenUtils.generateToken(userDetails);
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>(new UserErrorResponse("Invalid username or password", System.currentTimeMillis()), HttpStatus.BAD_REQUEST);
        }catch (DisabledException ex) {
            return new ResponseEntity<>(new UserErrorResponse("User account is disabled and should confirm email first", System.currentTimeMillis()), HttpStatus.BAD_REQUEST);
        }
        catch (CustomException ex) {
            return new ResponseEntity<>(new UserErrorResponse(ex.getMessage(), System.currentTimeMillis()), HttpStatus.BAD_REQUEST);
        }
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

    //Endpoint for email confirmation
    @GetMapping("/register/confirm")
    @Operation(summary = "Email confirmation", description = "Endpoint for activate account with email confirmation token.")
    public String confirmEmail(@RequestParam(name = "confirmToken", required = true) String confirmToken) {
        registrationService.activateAccount(confirmToken);
        return "Email confirmed. Please login.";
    }

    // Endpooint for confirmation token resending
    @PostMapping("/register/resendConfirmationToken")
    @Operation(summary = "Confirmation token resend", description = "Confirmation token resend")
    public ResponseEntity<?> resendConfirmToken(@RequestBody EmailResendDTO emailResendDTO) {
        registrationService.sendEmail(emailResendDTO.getEmail());
        return new ResponseEntity<>(Map.of("message", "Token resent"), HttpStatus.OK);
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

    @ExceptionHandler
    public ResponseEntity<?> RuntimeExceptionHandler(CustomException ex) {
        UserErrorResponse userErrorResponse = new UserErrorResponse(ex.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(userErrorResponse, HttpStatus.BAD_REQUEST);
    }
}
