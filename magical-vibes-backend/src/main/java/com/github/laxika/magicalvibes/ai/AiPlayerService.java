package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPlayerService {

    private final GameRegistry gameRegistry;
    private final GameService gameService;
    private final LobbyService lobbyService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void joinAsAi(GameData gameData, String aiDeckId) {
        UUID aiPlayerId = UUID.randomUUID();
        Player aiPlayer = new Player(aiPlayerId, "AI Opponent");

        AiDecisionEngine engine = new AiDecisionEngine(gameData.id, aiPlayer, gameRegistry, gameService);
        String connectionId = "ai-" + gameData.id;
        AiConnection aiConnection = new AiConnection(connectionId, engine, objectMapper);

        // Register the AI connection in the session manager so it receives messages
        sessionManager.registerPlayer(aiConnection, aiPlayerId, "AI Opponent");
        sessionManager.setInGame(connectionId);

        // Join the game — this triggers initializeGame() which sets status to MULLIGAN
        lobbyService.joinGame(gameData, aiPlayer, aiDeckId);

        // Schedule the AI's initial mulligan decision
        aiConnection.scheduleInitialAction(() -> engine.handleInitialMulligan(gameData));

        log.info("AI opponent joined game {} with deck {}", gameData.id, aiDeckId);
    }
}
