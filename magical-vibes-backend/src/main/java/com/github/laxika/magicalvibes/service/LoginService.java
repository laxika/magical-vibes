package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.PrebuiltDeck;
import com.github.laxika.magicalvibes.networking.message.DeckInfo;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.LoginResponse;
import com.github.laxika.magicalvibes.networking.message.SetInfo;
import com.github.laxika.magicalvibes.entity.User;
import com.github.laxika.magicalvibes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final LobbyService lobbyService;

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
            var games = lobbyService.listRunningGames();

            // Build available decks list
            List<DeckInfo> decks = Arrays.stream(PrebuiltDeck.values())
                    .map(d -> new DeckInfo(d.getId(), d.getName()))
                    .toList();

            // Build available sets list
            List<SetInfo> sets = Arrays.stream(CardSet.values())
                    .map(s -> new SetInfo(s.getCode(), s.getName()))
                    .toList();

            return LoginResponse.success(user.getId(), user.getUsername(), games, decks, sets, null);
        } else {
            log.warn("Authentication failed for username: {}", loginRequest.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
    }
}
