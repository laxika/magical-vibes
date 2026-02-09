package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.CreateGameRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.ErrorMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.JoinGameMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGameRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LobbyGame;
import com.github.laxika.magicalvibes.networking.message.LobbyGameMessage;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.LoginResponse;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.SetAutoStopsRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LoginService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

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
    public void handleLogin(Connection connection, LoginRequest request) throws Exception {
        LoginResponse response = loginService.authenticate(request);

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
    public void handleCreateGame(Connection connection, CreateGameRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameService.GameResult result = gameService.createGame(request.gameName(), player);

        // Mark creator as in-game
        sessionManager.setInGame(connection.getId());

        // Send GAME_JOINED to the creator
        sendJoinMessage(connection, MessageType.GAME_JOINED, result.joinGame());

        // Broadcast NEW_GAME to lobby users only
        broadcastToLobby(MessageType.NEW_GAME, result.lobbyGame());
    }

    @Override
    public void handleJoinGame(Connection connection, JoinGameRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            LobbyGame lobbyGame = gameService.joinGame(request.gameId(), player);

            // Mark joiner as in-game
            sessionManager.setInGame(connection.getId());

            // Send GAME_JOINED to the joiner (with their own hand)
            JoinGame joinerGame = gameService.getJoinGame(request.gameId(), player.getId());
            sendJoinMessage(connection, MessageType.GAME_JOINED, joinerGame);

            // Send OPPONENT_JOINED to the creator (with their own hand)
            Long creatorUserId = gameService.getCreatorUserId(request.gameId());
            if (creatorUserId != null) {
                Connection creatorConnection = sessionManager.getConnectionByUserId(creatorUserId);
                if (creatorConnection != null && creatorConnection.isOpen()) {
                    JoinGame creatorGame = gameService.getJoinGame(request.gameId(), creatorUserId);
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
    public void handlePassPriority(Connection connection, PassPriorityRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.passPriority(request.gameId(), player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleKeepHand(Connection connection, KeepHandRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.keepHand(request.gameId(), player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleMulligan(Connection connection, MulliganRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.mulligan(request.gameId(), player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleBottomCards(Connection connection, BottomCardsRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.bottomCards(request.gameId(), player, request.cardIndices());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handlePlayCard(Connection connection, PlayCardRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.playCard(request.gameId(), player, request.cardIndex());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleTapPermanent(Connection connection, TapPermanentRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.tapPermanent(request.gameId(), player, request.permanentIndex());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleSetAutoStops(Connection connection, SetAutoStopsRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.setAutoStops(request.gameId(), player, request.stops());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleDeclareAttackers(Connection connection, DeclareAttackersRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.declareAttackers(request.gameId(), player, request.attackerIndices());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleDeclareBlockers(Connection connection, DeclareBlockersRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.declareBlockers(request.gameId(), player, request.blockerAssignments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleCardChosen(Connection connection, CardChosenRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            gameService.handleCardChosen(request.gameId(), player, request.cardIndex());
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
