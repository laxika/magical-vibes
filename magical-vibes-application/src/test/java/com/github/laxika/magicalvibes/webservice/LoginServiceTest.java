package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.entity.User;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.LoginResponse;
import com.github.laxika.magicalvibes.networking.message.RegisterRequest;
import com.github.laxika.magicalvibes.networking.message.RegisterResponse;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import org.mockito.InjectMocks;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @Mock private UserRepository userRepository;
    @Mock private LobbyService lobbyService;
    @Mock private DeckService deckService;

    @InjectMocks private LoginService loginService;

    private RegisterRequest registerRequest(String username, String password, String confirmPassword) {
        return new RegisterRequest(MessageType.REGISTER, username, password, confirmPassword);
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates the user with a BCrypt-hashed password")
        void successfulRegistrationHashesPassword() {
            when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);

            RegisterResponse response = loginService.register(registerRequest("alice", "secret123", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_SUCCESS);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("alice");
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getPassword()).isNotEqualTo("secret123");
            assertThat(ENCODER.matches("secret123", saved.getPassword())).isTrue();
        }

        @Test
        @DisplayName("trims surrounding whitespace from the username")
        void trimsUsername() {
            when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);

            RegisterResponse response = loginService.register(registerRequest("  alice  ", "secret123", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_SUCCESS);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        }

        @Test
        @DisplayName("rejects usernames that are too short or contain illegal characters")
        void rejectsInvalidUsername() {
            for (String username : new String[]{"ab", "has space", "way-toooooo-long-username", "bad!chars", "", null}) {
                RegisterResponse response = loginService.register(registerRequest(username, "secret123", "secret123"));

                assertThat(response.getType()).as("username: %s", username).isEqualTo(MessageType.REGISTER_FAILURE);
            }
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("rejects passwords shorter than 6 characters")
        void rejectsShortPassword() {
            RegisterResponse response = loginService.register(registerRequest("alice", "short", "short"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_FAILURE);
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("rejects passwords longer than 72 bytes (BCrypt input limit)")
        void rejectsOverlongPassword() {
            String longPassword = "x".repeat(73);

            RegisterResponse response = loginService.register(registerRequest("alice", longPassword, longPassword));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_FAILURE);
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("rejects mismatched password confirmation")
        void rejectsMismatchedConfirmation() {
            RegisterResponse response = loginService.register(registerRequest("alice", "secret123", "secret124"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_FAILURE);
            assertThat(response.getMessage()).isEqualTo("Passwords do not match");
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("rejects an already-taken username (case-insensitive)")
        void rejectsTakenUsername() {
            when(userRepository.existsByUsernameIgnoreCase("Alice")).thenReturn(true);

            RegisterResponse response = loginService.register(registerRequest("Alice", "secret123", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_FAILURE);
            assertThat(response.getMessage()).isEqualTo("Username is already taken");
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("reports the username as taken when the unique constraint wins a registration race")
        void rejectsRaceLostOnUniqueConstraint() {
            when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
            when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));

            RegisterResponse response = loginService.register(registerRequest("alice", "secret123", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.REGISTER_FAILURE);
            assertThat(response.getMessage()).isEqualTo("Username is already taken");
        }
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("succeeds when the password matches the stored BCrypt hash")
        void successfulLogin() {
            User user = new User("alice", ENCODER.encode("secret123"));
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
            when(lobbyService.listRunningGames()).thenReturn(List.of());
            when(deckService.getCustomDecksForUser(user.getId())).thenReturn(List.of());

            LoginResponse response = loginService.authenticate(new LoginRequest(MessageType.LOGIN, "alice", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.LOGIN_SUCCESS);
            assertThat(response.getUserId()).isEqualTo(user.getId());
            assertThat(response.getUsername()).isEqualTo("alice");
        }

        @Test
        @DisplayName("fails when the password does not match the stored hash")
        void wrongPassword() {
            User user = new User("alice", ENCODER.encode("secret123"));
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

            LoginResponse response = loginService.authenticate(new LoginRequest(MessageType.LOGIN, "alice", "wrong"));

            assertThat(response.getType()).isEqualTo(MessageType.LOGIN_FAILURE);
        }

        @Test
        @DisplayName("fails for an unknown username")
        void unknownUser() {
            when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

            LoginResponse response = loginService.authenticate(new LoginRequest(MessageType.LOGIN, "nobody", "secret123"));

            assertThat(response.getType()).isEqualTo(MessageType.LOGIN_FAILURE);
        }

        @Test
        @DisplayName("fails without touching the repository when credentials are missing")
        void missingCredentials() {
            lenient().when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

            LoginResponse response = loginService.authenticate(new LoginRequest(MessageType.LOGIN, null, null));

            assertThat(response.getType()).isEqualTo(MessageType.LOGIN_FAILURE);
            verify(userRepository, never()).findByUsername(any());
        }
    }
}
