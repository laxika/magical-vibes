package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.entity.User;
import com.github.laxika.magicalvibes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // TODO: Implement password hashing with BCrypt for production use
    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest loginRequest) {
        logger.debug("Authentication attempt for username: {}", loginRequest.getUsername());

        Optional<User> userOptional = userRepository.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.info("Authentication successful for user: {}", user.getUsername());
            return LoginResponse.success(user.getId(), user.getUsername());
        } else {
            logger.warn("Authentication failed for username: {}", loginRequest.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
    }
}
