package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.dto.ErrorMessage;
import com.github.laxika.magicalvibes.dto.JoinGame;
import com.github.laxika.magicalvibes.dto.JoinGameMessage;
import com.github.laxika.magicalvibes.dto.LobbyGame;
import com.github.laxika.magicalvibes.dto.LobbyGameMessage;
import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LoginService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.github.laxika.magicalvibes.model.TurnStep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class LoginWebSocketHandler extends TextWebSocketHandler {

    private final LoginService loginService;
    private final GameService gameService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public LoginWebSocketHandler(LoginService loginService,
            GameService gameService,
            WebSocketSessionManager sessionManager,
            ObjectMapper objectMapper) {
        this.loginService = loginService;
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }


    private void sendLoginTimeout(WebSocketSession session) {
        LoginResponse timeoutResponse = LoginResponse.timeout();

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(timeoutResponse)));
            session.close(CloseStatus.NORMAL);
        } catch (IOException e) {
            log.error("Error sending timeout message", e);
        }
    }

    private void handleLogin(WebSocketSession session, JsonNode jsonNode) throws IOException {
        LoginRequest loginRequest = objectMapper.treeToValue(jsonNode, LoginRequest.class);
        LoginResponse response = loginService.authenticate(loginRequest);

        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
        log.info("Sent login response to session {}: {}", session.getId(), response.getType());

        if (response.getType() == MessageType.LOGIN_SUCCESS) {
            sessionManager.registerPlayer(session, response.getUserId(), response.getUsername());
            log.info("Session {} registered for user {} ({}) - connection staying open", session.getId(), response.getUserId(), response.getUsername());
        } else {
            session.close(CloseStatus.NORMAL);
        }
    }

    private void handleCreateGame(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        String gameName = jsonNode.get("gameName").asString();
        GameService.GameResult result = gameService.createGame(gameName, player);

        // Mark creator as in-game
        sessionManager.setInGame(session.getId());

        // Send GAME_JOINED to the creator
        sendJoinMessage(session, MessageType.GAME_JOINED, result.joinGame());

        // Broadcast NEW_GAME to lobby users only
        broadcastToLobby(MessageType.NEW_GAME, result.lobbyGame());
    }

    private void handleJoinGame(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            LobbyGame lobbyGame = gameService.joinGame(gameId, player);

            // Mark joiner as in-game
            sessionManager.setInGame(session.getId());

            // Send GAME_JOINED to the joiner (with their own hand)
            JoinGame joinerGame = gameService.getJoinGame(gameId, player.getId());
            sendJoinMessage(session, MessageType.GAME_JOINED, joinerGame);

            // Send OPPONENT_JOINED to the creator (with their own hand)
            Long creatorUserId = gameService.getCreatorUserId(gameId);
            if (creatorUserId != null) {
                WebSocketSession creatorSession = sessionManager.getSessionByUserId(creatorUserId);
                if (creatorSession != null && creatorSession.isOpen()) {
                    JoinGame creatorGame = gameService.getJoinGame(gameId, creatorUserId);
                    sendJoinMessage(creatorSession, MessageType.OPPONENT_JOINED, creatorGame);
                }
            }

            // Broadcast GAME_UPDATED to lobby users
            broadcastToLobby(MessageType.GAME_UPDATED, lobbyGame);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handlePassPriority(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.passPriority(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleKeepHand(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.keepHand(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleMulligan(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.mulligan(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleBottomCards(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        List<Integer> cardIndices = new ArrayList<>();
        JsonNode indicesNode = jsonNode.get("cardIndices");
        if (indicesNode != null && indicesNode.isArray()) {
            for (JsonNode idx : indicesNode) {
                cardIndices.add(idx.asInt());
            }
        }

        try {
            gameService.bottomCards(gameId, player, cardIndices);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handlePlayCard(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int cardIndex = jsonNode.get("cardIndex").asInt();

        try {
            gameService.playCard(gameId, player, cardIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleTapPermanent(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int permanentIndex = jsonNode.get("permanentIndex").asInt();

        try {
            gameService.tapPermanent(gameId, player, permanentIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleSetAutoStops(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        List<TurnStep> stops = new ArrayList<>();
        JsonNode stopsNode = jsonNode.get("stops");
        if (stopsNode != null && stopsNode.isArray()) {
            for (JsonNode stopNode : stopsNode) {
                stops.add(TurnStep.valueOf(stopNode.asString()));
            }
        }

        try {
            gameService.setAutoStops(gameId, player, stops);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleDeclareAttackers(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        List<Integer> attackerIndices = new ArrayList<>();
        JsonNode indicesNode = jsonNode.get("attackerIndices");
        if (indicesNode != null && indicesNode.isArray()) {
            for (JsonNode idx : indicesNode) {
                attackerIndices.add(idx.asInt());
            }
        }

        try {
            gameService.declareAttackers(gameId, player, attackerIndices);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleDeclareBlockers(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        List<int[]> blockerAssignments = new ArrayList<>();
        JsonNode assignmentsNode = jsonNode.get("blockerAssignments");
        if (assignmentsNode != null && assignmentsNode.isArray()) {
            for (JsonNode assignment : assignmentsNode) {
                int blockerIndex = assignment.get("blockerIndex").asInt();
                int attackerIndex = assignment.get("attackerIndex").asInt();
                blockerAssignments.add(new int[]{blockerIndex, attackerIndex});
            }
        }

        try {
            gameService.declareBlockers(gameId, player, blockerAssignments);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleCardChosen(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Player player = sessionManager.getPlayer(session.getId());
        if (player == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int cardIndex = jsonNode.get("cardIndex").asInt();

        try {
            gameService.handleCardChosen(gameId, player, cardIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void sendJoinMessage(WebSocketSession session, MessageType type, JoinGame game) throws IOException {
        JoinGameMessage message = new JoinGameMessage(type, game);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    private void broadcastToLobby(MessageType type, LobbyGame game) {
        LobbyGameMessage notification = new LobbyGameMessage(type, game);

        int sentCount = 0;

        for (Player player : sessionManager.getLobbyPlayers()) {
            try {
                WebSocketSession playerSession = sessionManager.getSessionByUserId(player.getId());
                if (playerSession != null && playerSession.isOpen()) {
                    String msg = objectMapper.writeValueAsString(notification);
                    playerSession.sendMessage(new TextMessage(msg));
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Error sending notification to player: {}", player.getUsername(), e);
            }
        }

        log.info("Broadcasted {} to {} lobby users", type, sentCount);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        ErrorMessage error = new ErrorMessage(message);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
    }
}
