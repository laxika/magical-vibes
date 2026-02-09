package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.dto.ErrorMessage;
import com.github.laxika.magicalvibes.dto.JoinGame;
import com.github.laxika.magicalvibes.dto.JoinGameMessage;
import com.github.laxika.magicalvibes.dto.LobbyGame;
import com.github.laxika.magicalvibes.dto.LobbyGameMessage;
import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LoginService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GameMessageHandler implements MessageHandler {

    private final LoginService loginService;
    private final GameService gameService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public GameMessageHandler(LoginService loginService,
            GameService gameService,
            WebSocketSessionManager sessionManager,
            ObjectMapper objectMapper) {
        this.loginService = loginService;
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleTimeout(Connection connection) {
        LoginResponse timeoutResponse = LoginResponse.timeout();

        try {
            connection.sendMessage(objectMapper.writeValueAsString(timeoutResponse));
            connection.close();
        } catch (Exception e) {
            log.error("Error sending timeout message", e);
        }
    }

    @Override
    public void handleLogin(Connection connection, JsonNode jsonNode) throws Exception {
        LoginRequest loginRequest = objectMapper.treeToValue(jsonNode, LoginRequest.class);
        LoginResponse response = loginService.authenticate(loginRequest);

        String jsonResponse = objectMapper.writeValueAsString(response);
        connection.sendMessage(jsonResponse);
        log.info("Sent login response to connection {}: {}", connection.getId(), response.getType());

        if (response.getType() == MessageType.LOGIN_SUCCESS) {
            sessionManager.registerPlayer(connection, response.getUserId(), response.getUsername());
            log.info("Connection {} registered for user {} ({}) - connection staying open", connection.getId(), response.getUserId(), response.getUsername());
        } else {
            connection.close();
        }
    }

    @Override
    public void handleCreateGame(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        String gameName = jsonNode.get("gameName").asString();
        GameService.GameResult result = gameService.createGame(gameName, player);

        // Mark creator as in-game
        sessionManager.setInGame(connection.getId());

        // Send GAME_JOINED to the creator
        sendJoinMessage(connection, MessageType.GAME_JOINED, result.joinGame());

        // Broadcast NEW_GAME to lobby users only
        broadcastToLobby(MessageType.NEW_GAME, result.lobbyGame());
    }

    @Override
    public void handleJoinGame(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            LobbyGame lobbyGame = gameService.joinGame(gameId, player);

            // Mark joiner as in-game
            sessionManager.setInGame(connection.getId());

            // Send GAME_JOINED to the joiner (with their own hand)
            JoinGame joinerGame = gameService.getJoinGame(gameId, player.getId());
            sendJoinMessage(connection, MessageType.GAME_JOINED, joinerGame);

            // Send OPPONENT_JOINED to the creator (with their own hand)
            Long creatorUserId = gameService.getCreatorUserId(gameId);
            if (creatorUserId != null) {
                Connection creatorConnection = sessionManager.getConnectionByUserId(creatorUserId);
                if (creatorConnection != null && creatorConnection.isOpen()) {
                    JoinGame creatorGame = gameService.getJoinGame(gameId, creatorUserId);
                    sendJoinMessage(creatorConnection, MessageType.OPPONENT_JOINED, creatorGame);
                }
            }

            // Broadcast GAME_UPDATED to lobby users
            broadcastToLobby(MessageType.GAME_UPDATED, lobbyGame);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handlePassPriority(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.passPriority(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleKeepHand(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.keepHand(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleMulligan(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            gameService.mulligan(gameId, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleBottomCards(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
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
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handlePlayCard(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int cardIndex = jsonNode.get("cardIndex").asInt();

        try {
            gameService.playCard(gameId, player, cardIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleTapPermanent(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int permanentIndex = jsonNode.get("permanentIndex").asInt();

        try {
            gameService.tapPermanent(gameId, player, permanentIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleSetAutoStops(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
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
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleDeclareAttackers(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
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
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleDeclareBlockers(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
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
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleCardChosen(Connection connection, JsonNode jsonNode) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();
        int cardIndex = jsonNode.get("cardIndex").asInt();

        try {
            gameService.handleCardChosen(gameId, player, cardIndex);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleError(Connection connection, String message) throws Exception {
        ErrorMessage error = new ErrorMessage(message);
        connection.sendMessage(objectMapper.writeValueAsString(error));
    }

    private void sendJoinMessage(Connection connection, MessageType type, JoinGame game) throws Exception {
        JoinGameMessage message = new JoinGameMessage(type, game);
        connection.sendMessage(objectMapper.writeValueAsString(message));
    }

    private void broadcastToLobby(MessageType type, LobbyGame game) {
        LobbyGameMessage notification = new LobbyGameMessage(type, game);

        int sentCount = 0;

        for (Player player : sessionManager.getLobbyPlayers()) {
            try {
                Connection playerConnection = sessionManager.getConnectionByUserId(player.getId());
                if (playerConnection != null && playerConnection.isOpen()) {
                    String msg = objectMapper.writeValueAsString(notification);
                    playerConnection.sendMessage(msg);
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Error sending notification to player: {}", player.getUsername(), e);
            }
        }

        log.info("Broadcasted {} to {} lobby users", type, sentCount);
    }
}
