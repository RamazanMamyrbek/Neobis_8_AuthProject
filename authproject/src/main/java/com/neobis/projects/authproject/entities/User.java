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

    @Column(name = "email")
    private String email;

    @Column(name = "login")
    private String username;

    @Column(name = "password")
    private String password;



    @Enumerated(EnumType.STRING)
    @Column(name = "is_first_time")
    private UserStatus isFirstTime;

    @Column(name = "is_logged_in")
    private boolean isLoggedIn;
}
