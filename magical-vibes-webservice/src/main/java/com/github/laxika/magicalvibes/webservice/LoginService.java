package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.PrebuiltDeck;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.networking.message.DeckInfo;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.LoginResponse;
import com.github.laxika.magicalvibes.networking.message.RegisterRequest;
import com.github.laxika.magicalvibes.networking.message.RegisterResponse;
import com.github.laxika.magicalvibes.networking.message.SetInfo;
import com.github.laxika.magicalvibes.entity.User;
import com.github.laxika.magicalvibes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    // BCrypt only hashes the first 72 bytes of input; reject anything longer
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final LobbyService lobbyService;
    private final DeckService deckService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.debug("Authentication attempt for username: {}", loginRequest.getUsername());

        Optional<User> userOptional = loginRequest.getUsername() == null || loginRequest.getPassword() == null
                ? Optional.empty()
                : userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            User user = userOptional.get();
            log.info("Authentication successful for user: {}", user.getUsername());

            // Fetch available games
            var games = lobbyService.listRunningGames();

            // Build available decks list (custom decks first, then prebuilt)
            List<DeckInfo> customDecks = deckService.getCustomDecksForUser(user.getId());
            List<DeckInfo> prebuiltDecks = Arrays.stream(PrebuiltDeck.values())
                    .map(d -> new DeckInfo(d.getId(), d.getDisplayName()))
                    .toList();
            List<DeckInfo> decks = new ArrayList<>();
            decks.addAll(customDecks);
            decks.addAll(prebuiltDecks);

            // Build available sets list, flagging those complete enough for set-restricted random decks
            List<SetInfo> sets = Arrays.stream(CardSet.values())
                    .map(s -> new SetInfo(s.getCode(), s.getName(), RandomDeckGenerator.isSetRandomEligible(s)))
                    .toList();

            return LoginResponse.success(user.getId(), user.getUsername(), games, decks, sets, null);
        } else {
            log.warn("Authentication failed for username: {}", loginRequest.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return RegisterResponse.failure("Username must be 3-20 characters using only letters, numbers, '_' or '-'");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return RegisterResponse.failure("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            return RegisterResponse.failure("Password is too long");
        }
        if (!password.equals(confirmPassword)) {
            return RegisterResponse.failure("Passwords do not match");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return RegisterResponse.failure("Username is already taken");
        }

        try {
            userRepository.saveAndFlush(new User(username, passwordEncoder.encode(password)));
        } catch (DataIntegrityViolationException e) {
            // Concurrent registration with the same username lost the race on the unique constraint
            return RegisterResponse.failure("Username is already taken");
        }

        log.info("Registered new user: {}", username);
        return RegisterResponse.success();
    }
}
