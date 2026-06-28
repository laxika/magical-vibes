package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.config.GameEngineConfig;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Headless simulation bindings on top of {@link GameEngineConfig}: no database, no real
 * WebSocket broadcasts, and an isolated {@link GameRegistry} so MCTS never touches live games.
 */
@Configuration
@Import({GameEngineConfig.class, JacksonConfig.class})
public class HeadlessSimulationDoublesConfig {

    @Bean
    @Primary
    GameRegistry gameRegistry() {
        return new GameRegistry();
    }

    @Bean
    @Primary
    HeadlessWebSocketSessionManager webSocketSessionManager(ObjectMapper objectMapper) {
        return new HeadlessWebSocketSessionManager(objectMapper);
    }

    /**
     * Headless contexts have no {@code @Value} property resolution; provide explicit timeouts
     * and omit the outcome collaborator (timers are never started in simulation).
     */
    @Bean
    @Primary
    GameTimeoutService gameTimeoutService(GameRegistry gameRegistry,
                                          HeadlessWebSocketSessionManager webSocketSessionManager) {
        return new GameTimeoutService(
                gameRegistry, null, webSocketSessionManager, Duration.ofMinutes(5), Duration.ofMinutes(15));
    }
}
