package com.neobis.projects.authproject.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.neobis.projects.authproject.dto.UserErrorResponse;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.services.UserService;
import com.neobis.projects.authproject.utils.UserLoggedOutException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if(token.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Jwt token is empty");
                log.info("JWT Token is blank");
                return;
            } else {
                try {
                    String username = jwtTokenUtils.validateAndExtractUsernameFromToken(token);
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    User currentUser = userService.findByUsername(username).get();
                    if(!currentUser.isLoggedIn()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("User is should login first");
                        return;
                    }
                    if(!currentUser.getEnabled()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("User should confirm email first");
                        return;
                    }

                    if(SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()));
                    }
                } catch (JWTVerificationException ex) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Invalid JWT token");
                    log.info("Invalid JWT TOKEN");
                    return;
                } catch (UsernameNotFoundException ex) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Username not found");
                    log.info("Username not found");
                    return;
                } catch (UserLoggedOutException ex) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("User should login first");
                    log.info(ex.getMessage());
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
