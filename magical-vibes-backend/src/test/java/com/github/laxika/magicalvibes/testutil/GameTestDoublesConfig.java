package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.ai.AiPlayerService;
import com.github.laxika.magicalvibes.config.GameEngineConfig;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.handler.GameMessageHandler;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.repository.DeckRepository;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.webservice.LobbyService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.webservice.CardBrowserService;
import com.github.laxika.magicalvibes.webservice.DeckService;
import com.github.laxika.magicalvibes.webservice.DraftService;
import com.github.laxika.magicalvibes.webservice.LoginService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Test-only beans layered on top of {@link GameEngineConfig}. Mirrors the deliberate null/stub
 * collaborators that {@code GameTestHarness} used when wiring manually: no database, no real
 * login/draft/deck browser, and timers that never fire in card tests.
 */
@Configuration
@Import({GameEngineConfig.class, JacksonConfig.class})
public class GameTestDoublesConfig {

    @Bean
    @Primary
    TestGameRegistry gameRegistry() {
        return new TestGameRegistry();
    }

    @Bean
    @Primary
    TestWebSocketSessionManager webSocketSessionManager(ObjectMapper objectMapper) {
        return new TestWebSocketSessionManager(objectMapper);
    }

    /**
     * Harness used {@code new DeckService(null, null)} so lobby/deck listing never touches JPA.
     */
    @Bean
    @Primary
    DeckService deckService() {
        return new DeckService((DeckRepository) null, (ObjectMapper) null);
    }

    /**
     * {@code LobbyService} now lives in the application layer (outside the engine scan), so the
     * test context provides it explicitly for the message handler and harness.
     */
    @Bean
    LobbyService lobbyService(TestGameRegistry gameRegistry, GameBroadcastService gameBroadcastService) {
        return new LobbyService(gameRegistry, gameBroadcastService, deckService());
    }

    @Bean
    @Primary
    LoginService loginService() {
        return Mockito.mock(LoginService.class);
    }

    @Bean
    @Primary
    CardBrowserService cardBrowserService() {
        return Mockito.mock(CardBrowserService.class);
    }

    /**
     * Timers never fire in card tests; outcome service is only used outside timer callbacks.
     * Mirrors manual construction with a null outcome collaborator on the timeout service.
     */
    @Bean
    @Primary
    GameTimeoutService gameTimeoutService(TestGameRegistry gameRegistry,
                                          TestWebSocketSessionManager webSocketSessionManager) {
        return new GameTimeoutService(
                gameRegistry, null, webSocketSessionManager, Duration.ofMinutes(5), Duration.ofMinutes(15));
    }

    @Bean
    MessageHandler messageHandler(GameService gameService,
                                  GameBroadcastService gameBroadcastService,
                                  LobbyService lobbyService,
                                  GameRegistry gameRegistry,
                                  TestWebSocketSessionManager webSocketSessionManager,
                                  ObjectMapper objectMapper,
                                  DraftRegistry draftRegistry,
                                  ValidTargetService validTargetService) {
        return new GameMessageHandler(
                (LoginService) null,
                gameService,
                gameBroadcastService,
                lobbyService,
                gameRegistry,
                webSocketSessionManager,
                objectMapper,
                (AiPlayerService) null,
                (DraftService) null,
                draftRegistry,
                (CardBrowserService) null,
                validTargetService,
                deckService(),
                (GameTimeoutService) null);
    }
}
