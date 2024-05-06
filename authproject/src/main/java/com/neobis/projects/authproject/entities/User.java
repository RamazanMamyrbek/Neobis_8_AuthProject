package com.neobis.projects.authproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//User table
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "login", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    private Boolean enabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_first_time")
    private UserStatus isFirstTime;

    @Column(name = "is_logged_in")
    private boolean isLoggedIn;
}
