package com.neobis.projects.authproject.controllers;


import com.neobis.projects.authproject.dto.UserLoginDTO;
import com.neobis.projects.authproject.dto.UserRegistrationDTO;
import com.neobis.projects.authproject.services.RegistrationService;
import com.neobis.projects.authproject.services.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "Controller for authentication/authorization")
public class AuthController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final AuthenticationProvider authenticationProvider;


    // Endpoint for registration: /api/register
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Endpoint for user registration. Returns jwt token.")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDTO userRegistrationDTO) {
        registrationService.registerUser(userRegistrationDTO);
        return new ResponseEntity<>("Registration...", HttpStatus.CREATED);
    }


    // Endpoint for login: /api/login
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Endpoint for user login. Returns jwt token.")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),
                userLoginDTO.getPassword());
        try{
            authenticationProvider.authenticate(authentication);
        }catch (BadCredentialsException ex) {
            return new ResponseEntity<>("Bad credentials...", HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>("Login...", HttpStatus.OK);
    }

    // Endpoint for Home page
    @GetMapping("/me")
    @Operation(summary = "Home page", description = "Home page is available after registration.")
    public ResponseEntity<String> homePage() {
        return new ResponseEntity<>("Home page", HttpStatus.OK);
    }

    // Endpoint for logout
    @GetMapping("/logout")
    @Operation(summary = "User logout", description = "Endpoint for user logout.")
    public ResponseEntity<String> logout() {
    return new ResponseEntity<>("Logout...", HttpStatus.OK);
    }
}
