package com.neobis.projects.authproject.services;

import com.neobis.projects.authproject.dto.UserRegistrationDTO;
import com.neobis.projects.authproject.entities.ConfirmationToken;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.entities.UserStatus;
import com.neobis.projects.authproject.repositories.ConfirmationTokenRepository;
import com.neobis.projects.authproject.repositories.UserRepository;
import com.neobis.projects.authproject.utils.CustomException;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;

    private String link = "https://165.227.147.154:8081/api/register/confirm?confirmToken=";

    @Transactional
    public void registerUser(UserRegistrationDTO userRegistrationDTO) {
        User user = modelMapper.map(userRegistrationDTO, User.class);
        user.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
        user.setIsFirstTime(UserStatus.FIRST_TIME);
        user.setLoggedIn(true);
        log.info(user.toString());

        sendEmail(user);


        userRepository.save(user);
    }

    @Transactional
    public void activateAccount(String confirmToken) {
        confirmationTokenService.confirmToken(confirmToken);
    }

    @Transactional
    public void sendEmail(User user) {
        String confirmTokenText = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(confirmTokenText)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .build();
        confirmationTokenService.saveToken(confirmationToken);
        String completelink = link + confirmTokenText;
        try {
            emailSenderService.sendEmail(user.getEmail(), "Email confirmation", completelink);
        } catch (Exception ex) {
            throw new CustomException("Email not found");
        }
    }

    @Transactional
    public void sendEmail(String email) {
        try {
            String confirmTokenText = UUID.randomUUID().toString();

            String completelink = link + confirmTokenText;
            User user = userRepository.findUserByEmail(email).get();
            confirmationTokenService.removeAllTokensFromUser(user);
            ConfirmationToken confirmationToken = ConfirmationToken.builder()
                    .token(confirmTokenText)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .user(user)
                    .build();

            confirmationTokenService.saveToken(confirmationToken);
            emailSenderService.sendEmail(email, "Email confirmation", completelink);
        } catch (Exception ex) {
            throw new CustomException("Email not found");
        }
    }
}
