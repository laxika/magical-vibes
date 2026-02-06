package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.entity.User;
import com.github.laxika.magicalvibes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final GameService gameService;

    // TODO: Implement password hashing with BCrypt for production use
    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.debug("Authentication attempt for username: {}", loginRequest.getUsername());

        Optional<User> userOptional = userRepository.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("Authentication successful for user: {}", user.getUsername());

            // Fetch available games
            var games = gameService.listRunningGames();

            return LoginResponse.success(user.getId(), user.getUsername(), games);
        } else {
            log.warn("Authentication failed for username: {}", loginRequest.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
    }
}
