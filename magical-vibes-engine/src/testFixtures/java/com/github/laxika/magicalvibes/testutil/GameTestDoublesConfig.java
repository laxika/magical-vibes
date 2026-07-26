package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.service.GameEngineConfig;
import com.github.laxika.magicalvibes.service.GameSessionTransportAdapter;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Engine-level test wiring layered on top of {@link GameEngineConfig}: an in-memory game registry,
 * a no-op session manager, and a timeout service whose timers never fire. {@code GameSetupService}
 * comes from the engine config's own component scan. No application
 * (backend) beans are involved — the shared harness drives games directly through engine services,
 * so it has no need for the message handler, login, draft, or deck services.
 */
@Configuration
@Import(GameEngineConfig.class)
public class GameTestDoublesConfig {

    @Bean
    @Primary
    TestGameRegistry gameRegistry() {
        return new TestGameRegistry();
    }

    @Bean
    @Primary
    TestWebSocketSessionManager webSocketSessionManager() {
        return new TestWebSocketSessionManager();
    }

    @Bean
    TestGameEventSubscriber testGameEventSubscriber() {
        return new TestGameEventSubscriber();
    }

    /**
     * Timers never fire in card tests; the outcome service is only used outside timer callbacks, so
     * a null outcome collaborator with explicit timeouts mirrors the production wiring closely enough.
     */
    @Bean
    @Primary
    GameTimeoutService gameTimeoutService(TestGameRegistry gameRegistry,
                                          GameSessionTransportAdapter sessionTransport,
                                          GameMutationCoordinator mutationCoordinator) {
        return new GameTimeoutService(
                gameRegistry, null, sessionTransport, mutationCoordinator,
                Duration.ofMinutes(5), Duration.ofMinutes(15));
    }
}
