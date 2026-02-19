package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.service.GameQueryService;
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
    private final LobbyService lobbyService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public AiPlayerService(GameRegistry gameRegistry,
                           ObjectProvider<MessageHandler> messageHandlerProvider,
                           GameQueryService gameQueryService,
                           LobbyService lobbyService,
                           WebSocketSessionManager sessionManager,
                           ObjectMapper objectMapper) {
        this.gameRegistry = gameRegistry;
        this.messageHandlerProvider = messageHandlerProvider;
        this.gameQueryService = gameQueryService;
        this.lobbyService = lobbyService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void joinAsAi(GameData gameData, String aiDeckId) {
        UUID aiPlayerId = UUID.randomUUID();
        Player aiPlayer = new Player(aiPlayerId, "AI Opponent");

        MessageHandler handler = messageHandlerProvider.getObject();
        AiDecisionEngine engine = new AiDecisionEngine(gameData.id, aiPlayer, gameRegistry, handler, gameQueryService);
        String connectionId = "ai-" + gameData.id;
        AiConnection aiConnection = new AiConnection(connectionId, engine, objectMapper);
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
