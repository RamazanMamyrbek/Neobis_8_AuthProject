package com.neobis.projects.authproject.services;

import com.neobis.projects.authproject.entities.ConfirmationToken;
import com.neobis.projects.authproject.entities.User;
import com.neobis.projects.authproject.repositories.ConfirmationTokenRepository;
import com.neobis.projects.authproject.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    @Transactional
    public void confirmToken(String token) {
        ConfirmationToken confirmToken = confirmationTokenRepository.findConfirmationTokenByToken(token);
        if(confirmToken == null) {
            throw new CustomException("Invalid confirmation token.");
        }
        if(LocalDateTime.now().isAfter(confirmToken.getExpiresAt())) {
            throw new CustomException("Token expired");
        }
        confirmToken.setConfirmedAt(LocalDateTime.now());
        User user = confirmToken.getUser();
        user.setEnabled(true);
        confirmationTokenRepository.save(confirmToken);
    }

    @Transactional
    public void saveToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    @Transactional
    public void removeAllTokensFromUser(User user) {
        confirmationTokenRepository.removeAllByUser(user);
    }
}
