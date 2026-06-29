package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.service.GameEngineConfig;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Headless simulation bindings on top of {@link GameEngineConfig}: no database, no real
 * WebSocket broadcasts, and an isolated {@link GameRegistry} so MCTS never touches live games.
 *
 * <p>Intentionally <strong>not</strong> annotated with {@code @Configuration}: this class lives
 * under the backend's component-scan path ({@code ...ai}), but it must only be loaded explicitly by
 * {@link HeadlessSimulationContext} via {@code AnnotationConfigApplicationContext}. Dropping the
 * {@code @Configuration} (meta-{@code @Component}) stereotype keeps the component scanner from
 * registering it in the main application context — where its {@code @Bean webSocketSessionManager}
 * would collide with the real {@code WebSocketSessionManager} {@code @Service}. Explicit
 * registration still processes the {@code @Import}/{@code @Bean} methods in lite mode, which is
 * sufficient here because none of these {@code @Bean} methods call one another.
 */
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
