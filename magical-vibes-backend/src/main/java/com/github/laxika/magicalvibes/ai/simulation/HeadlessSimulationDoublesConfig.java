package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.config.GameEngineConfig;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.repository.DeckRepository;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.webservice.DeckService;
import com.github.laxika.magicalvibes.webservice.DraftService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.springframework.beans.factory.ObjectProvider;
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

    @Bean
    @Primary
    DeckService deckService() {
        return new DeckService((DeckRepository) null, (ObjectMapper) null);
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

    @Bean
    @Primary
    DraftService draftService(DraftRegistry draftRegistry,
                              GameRegistry gameRegistry,
                              GameBroadcastService gameBroadcastService,
                              ObjectProvider<MessageHandler> messageHandlerProvider,
                              GameQueryService gameQueryService,
                              CombatAttackService combatAttackService,
                              SessionManager sessionManager,
                              WebSocketSessionManager webSocketSessionManager,
                              CardViewFactory cardViewFactory,
                              TargetValidationService targetValidationService,
                              TargetLegalityService targetLegalityService,
                              ObjectMapper objectMapper) {
        return new DraftService(
                draftRegistry,
                gameRegistry,
                gameBroadcastService,
                messageHandlerProvider,
                gameQueryService,
                combatAttackService,
                sessionManager,
                webSocketSessionManager,
                cardViewFactory,
                targetValidationService,
                targetLegalityService,
                objectMapper);
    }
}
