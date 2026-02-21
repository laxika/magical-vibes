package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ColorChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MayAbilityChosenRequest;
import com.github.laxika.magicalvibes.networking.message.CreateGameRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.JoinGameRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LoginRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.MultipleGraveyardCardsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultiplePermanentsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsRequest;
import com.github.laxika.magicalvibes.networking.message.SacrificePermanentRequest;
import com.github.laxika.magicalvibes.networking.message.SetAutoStopsRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.networking.message.HandTopBottomChosenRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.CreateDraftRequest;
import com.github.laxika.magicalvibes.networking.message.DraftPickRequest;
import com.github.laxika.magicalvibes.networking.message.SubmitDeckRequest;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final int TIMEOUT_SECONDS = 3;

    private final ObjectMapper objectMapper;
    private final MessageHandler messageHandler;
    private final WebSocketSessionManager sessionManager;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());

        Connection connection = new WebSocketConnection(session);

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                if (connection.isOpen()) {
                    log.warn("Connection timeout for session: {}", session.getId());
                    messageHandler.handleTimeout(connection);
                }
            } finally {
                timeoutTasks.remove(session.getId());
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

        timeoutTasks.put(session.getId(), timeoutTask);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received message from session {}: {}", session.getId(), message.getPayload());

        // Cancel timeout task on first message
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(session.getId());
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }

        Connection connection = new WebSocketConnection(session);

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            MessageType type = MessageType.valueOf(jsonNode.get("type").asString());

            switch (type) {
                case LOGIN -> messageHandler.handleLogin(connection, objectMapper.treeToValue(jsonNode, LoginRequest.class));
                case CREATE_GAME -> messageHandler.handleCreateGame(connection, objectMapper.treeToValue(jsonNode, CreateGameRequest.class));
                case JOIN_GAME -> messageHandler.handleJoinGame(connection, objectMapper.treeToValue(jsonNode, JoinGameRequest.class));
                case PASS_PRIORITY -> messageHandler.handlePassPriority(connection, objectMapper.treeToValue(jsonNode, PassPriorityRequest.class));
                case KEEP_HAND -> messageHandler.handleKeepHand(connection, objectMapper.treeToValue(jsonNode, KeepHandRequest.class));
                case TAKE_MULLIGAN -> messageHandler.handleMulligan(connection, objectMapper.treeToValue(jsonNode, MulliganRequest.class));
                case BOTTOM_CARDS -> messageHandler.handleBottomCards(connection, objectMapper.treeToValue(jsonNode, BottomCardsRequest.class));
                case PLAY_CARD -> messageHandler.handlePlayCard(connection, objectMapper.treeToValue(jsonNode, PlayCardRequest.class));
                case TAP_PERMANENT -> messageHandler.handleTapPermanent(connection, objectMapper.treeToValue(jsonNode, TapPermanentRequest.class));
                case SACRIFICE_PERMANENT -> messageHandler.handleSacrificePermanent(connection, objectMapper.treeToValue(jsonNode, SacrificePermanentRequest.class));
                case ACTIVATE_ABILITY -> messageHandler.handleActivateAbility(connection, objectMapper.treeToValue(jsonNode, ActivateAbilityRequest.class));
                case SET_AUTO_STOPS -> messageHandler.handleSetAutoStops(connection, objectMapper.treeToValue(jsonNode, SetAutoStopsRequest.class));
                case DECLARE_ATTACKERS -> messageHandler.handleDeclareAttackers(connection, objectMapper.treeToValue(jsonNode, DeclareAttackersRequest.class));
                case DECLARE_BLOCKERS -> messageHandler.handleDeclareBlockers(connection, objectMapper.treeToValue(jsonNode, DeclareBlockersRequest.class));
                case CARD_CHOSEN -> messageHandler.handleCardChosen(connection, objectMapper.treeToValue(jsonNode, CardChosenRequest.class));
                case GRAVEYARD_CARD_CHOSEN -> messageHandler.handleGraveyardCardChosen(connection, objectMapper.treeToValue(jsonNode, GraveyardCardChosenRequest.class));
                case PERMANENT_CHOSEN -> messageHandler.handlePermanentChosen(connection, objectMapper.treeToValue(jsonNode, PermanentChosenRequest.class));
                case MULTIPLE_PERMANENTS_CHOSEN -> messageHandler.handleMultiplePermanentsChosen(connection, objectMapper.treeToValue(jsonNode, MultiplePermanentsChosenRequest.class));
                case MULTIPLE_GRAVEYARD_CARDS_CHOSEN -> messageHandler.handleMultipleGraveyardCardsChosen(connection, objectMapper.treeToValue(jsonNode, MultipleGraveyardCardsChosenRequest.class));
                case COLOR_CHOSEN -> messageHandler.handleColorChosen(connection, objectMapper.treeToValue(jsonNode, ColorChosenRequest.class));
                case MAY_ABILITY_CHOSEN -> messageHandler.handleMayAbilityChosen(connection, objectMapper.treeToValue(jsonNode, MayAbilityChosenRequest.class));
                case LIBRARY_CARDS_REORDERED -> messageHandler.handleLibraryCardsReordered(connection, objectMapper.treeToValue(jsonNode, ReorderLibraryCardsRequest.class));
                case LIBRARY_CARD_CHOSEN -> messageHandler.handleLibraryCardChosen(connection, objectMapper.treeToValue(jsonNode, LibraryCardChosenRequest.class));
                case HAND_TOP_BOTTOM_CHOSEN -> messageHandler.handleHandTopBottomChosen(connection, objectMapper.treeToValue(jsonNode, HandTopBottomChosenRequest.class));
                case CREATE_DRAFT -> messageHandler.handleCreateDraft(connection, objectMapper.treeToValue(jsonNode, CreateDraftRequest.class));
                case DRAFT_PICK -> messageHandler.handleDraftPick(connection, objectMapper.treeToValue(jsonNode, DraftPickRequest.class));
                case SUBMIT_DECK -> messageHandler.handleSubmitDeck(connection, objectMapper.treeToValue(jsonNode, SubmitDeckRequest.class));
                case COMBAT_DAMAGE_ASSIGNED -> messageHandler.handleCombatDamageAssigned(connection, objectMapper.treeToValue(jsonNode, CombatDamageAssignedRequest.class));
                default -> messageHandler.handleError(connection, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            try {
                messageHandler.handleError(connection, "Error processing request");
            } catch (Exception ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        sessionManager.unregisterSession(session.getId());

        ScheduledFuture<?> task = timeoutTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        sessionManager.unregisterSession(session.getId());

        ScheduledFuture<?> task = timeoutTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
    }
}
