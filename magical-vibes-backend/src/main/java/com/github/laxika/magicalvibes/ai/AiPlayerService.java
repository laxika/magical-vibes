package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.AiDifficulty;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
public class AiPlayerService {

    private final GameRegistry gameRegistry;
    private final ObjectProvider<MessageHandler> messageHandlerProvider;
    private final GameQueryService gameQueryService;
    private final CombatAttackService combatAttackService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetValidationService targetValidationService;
    private final TargetLegalityService targetLegalityService;
    private final LobbyService lobbyService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public AiPlayerService(GameRegistry gameRegistry,
                           ObjectProvider<MessageHandler> messageHandlerProvider,
                           GameQueryService gameQueryService,
                           CombatAttackService combatAttackService,
                           GameBroadcastService gameBroadcastService,
                           TargetValidationService targetValidationService,
                           TargetLegalityService targetLegalityService,
                           LobbyService lobbyService,
                           WebSocketSessionManager sessionManager,
                           ObjectMapper objectMapper) {
        this.gameRegistry = gameRegistry;
        this.messageHandlerProvider = messageHandlerProvider;
        this.gameQueryService = gameQueryService;
        this.combatAttackService = combatAttackService;
        this.gameBroadcastService = gameBroadcastService;
        this.targetValidationService = targetValidationService;
        this.targetLegalityService = targetLegalityService;
        this.lobbyService = lobbyService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void joinAsAi(GameData gameData, String aiDeckId) {
        joinAsAi(gameData, aiDeckId, AiDifficulty.EASY);
    }

    public void joinAsAi(GameData gameData, String aiDeckId, AiDifficulty aiDifficulty) {
        if (aiDifficulty == null) {
            aiDifficulty = AiDifficulty.EASY;
        }
        String aiName = "AI Opponent (" + aiDifficulty.getDisplayName() + ")";
        UUID aiPlayerId = UUID.randomUUID();
        Player aiPlayer = new Player(aiPlayerId, aiName);

        MessageHandler handler = messageHandlerProvider.getObject();
        AiDecisionEngine engine = switch (aiDifficulty) {
            case HARD -> new HardAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, handler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
            case MEDIUM -> new MediumAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, handler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
            case EASY -> new EasyAiDecisionEngine(gameData.id, aiPlayer, gameRegistry, handler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
        };
        String connectionId = "ai-" + gameData.id;
        AiConnection aiConnection = new AiConnection(connectionId, engine, objectMapper, aiDifficulty.getDecisionDelayMs());
        engine.setSelfConnection(aiConnection);

        // Register the AI connection in the session manager so it receives messages
        sessionManager.registerPlayer(aiConnection, aiPlayerId, "AI Opponent");
        sessionManager.setInGame(connectionId);

        // Join the game — this triggers initializeGame() which sets status to MULLIGAN
        lobbyService.joinGame(gameData, aiPlayer, aiDeckId);

        // Schedule the AI's initial mulligan decision
        aiConnection.scheduleInitialAction(engine::handleInitialMulligan);

        log.info("AI opponent joined game {} with deck {}", gameData.id, aiDeckId);
    }
}

