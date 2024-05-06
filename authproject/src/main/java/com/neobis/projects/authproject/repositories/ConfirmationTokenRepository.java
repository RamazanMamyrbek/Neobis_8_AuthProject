package com.neobis.projects.authproject.repositories;

import com.neobis.projects.authproject.entities.ConfirmationToken;
import com.neobis.projects.authproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    ConfirmationToken findConfirmationTokenByToken(String token);

    void removeAllByUser(User user);
}
