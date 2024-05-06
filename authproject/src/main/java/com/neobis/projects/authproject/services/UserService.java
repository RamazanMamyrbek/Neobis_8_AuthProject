package com.neobis.projects.authproject.services;

import com.neobis.projects.authproject.dto.UserRegistrationDTO;
import com.neobis.projects.authproject.entities.MyUserDetails;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.entities.UserStatus;
import com.neobis.projects.authproject.repositories.UserRepository;
import com.neobis.projects.authproject.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = findByUsername(username);
        if(user.isEmpty()) {
            throw new UsernameNotFoundException("Username not found.");
        }
        return new MyUserDetails(user.get());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional
    public void setUserStatus(User user, UserStatus userStatus) {
        User userToBeUpdated = userRepository.findUserByUsername(user.getUsername()).get();
        userToBeUpdated.setIsFirstTime(userStatus);
        userRepository.save(userToBeUpdated);
    }

    @Transactional
    public void logoutUser(String username) {
        User user = userRepository.findUserByUsername(username).get();
        user.setLoggedIn(false);
    }

    @Transactional
    public void loginUser(String username) {
        User user = userRepository.findUserByUsername(username).get();
        user.setLoggedIn(true);
    }
}
