package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.model.AiDifficulty;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.InteractionAnswerRequest;
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
import com.github.laxika.magicalvibes.networking.message.PaySearchTaxRequest;
import com.github.laxika.magicalvibes.networking.message.RevertManaActivationsRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.RegisterRequest;
import com.github.laxika.magicalvibes.networking.message.RegisterResponse;
import com.github.laxika.magicalvibes.networking.message.SaveDeckRequest;
import com.github.laxika.magicalvibes.networking.message.SaveDeckResponse;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateGraveyardAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateHandAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.SacrificePermanentRequest;
import com.github.laxika.magicalvibes.networking.message.SetAutoStopsRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.networking.message.CardListResponse;
import com.github.laxika.magicalvibes.networking.message.CreateDraftRequest;
import com.github.laxika.magicalvibes.networking.message.DraftPickRequest;
import com.github.laxika.magicalvibes.networking.message.RequestCardListRequest;
import com.github.laxika.magicalvibes.networking.message.SubmitDeckRequest;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsRequest;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.ai.AiPlayerService;
import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.DraftStatus;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.webservice.DraftService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.webservice.CardBrowserService;
import com.github.laxika.magicalvibes.webservice.DeckService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.PlayCardRequestDispatchService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.webservice.LobbyService;
import com.github.laxika.magicalvibes.webservice.LoginService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class GameMessageHandler implements MessageHandler {

    private final LoginService loginService;
    private final GameService gameService;
    private final GameBroadcastService gameBroadcastService;
    private final LobbyService lobbyService;
    private final GameRegistry gameRegistry;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final AiPlayerService aiPlayerService;
    private final DraftService draftService;
    private final DraftRegistry draftRegistry;
    private final CardBrowserService cardBrowserService;
    private final ValidTargetService validTargetService;
    private final DeckService deckService;
    private final GameTimeoutService gameTimeoutService;
    private final PlayCardRequestDispatchService playCardRequestDispatchService;

    public GameMessageHandler(LoginService loginService,
            GameService gameService,
            GameBroadcastService gameBroadcastService,
            LobbyService lobbyService,
            GameRegistry gameRegistry,
            WebSocketSessionManager sessionManager,
            ObjectMapper objectMapper,
            AiPlayerService aiPlayerService,
            DraftService draftService,
            DraftRegistry draftRegistry,
            CardBrowserService cardBrowserService,
            ValidTargetService validTargetService,
            DeckService deckService,
            GameTimeoutService gameTimeoutService,
            PlayCardRequestDispatchService playCardRequestDispatchService) {
        this.loginService = loginService;
        this.gameService = gameService;
        this.gameBroadcastService = gameBroadcastService;
        this.lobbyService = lobbyService;
        this.gameRegistry = gameRegistry;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.aiPlayerService = aiPlayerService;
        this.draftService = draftService;
        this.draftRegistry = draftRegistry;
        this.cardBrowserService = cardBrowserService;
        this.validTargetService = validTargetService;
        this.deckService = deckService;
        this.gameTimeoutService = gameTimeoutService;
        this.playCardRequestDispatchService = playCardRequestDispatchService;
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

        if (response.getType() == MessageType.LOGIN_SUCCESS) {
            // Check if the player has an active game to rejoin
            GameData activeGame = gameRegistry.getGameForPlayer(response.getUserId());
            if (activeGame != null) {
                JoinGame joinGame = gameBroadcastService.getJoinGame(activeGame, response.getUserId());
                response.setActiveGame(joinGame);
            }

            // Check if the player has an active draft to rejoin
            DraftData activeDraft = draftRegistry.getDraftForPlayer(response.getUserId());
            if (activeDraft != null) {
                response.setActiveDraftId(activeDraft.id);
            }
        }

        String jsonResponse = objectMapper.writeValueAsString(response);
        connection.sendMessage(jsonResponse);
        log.info("Sent login response to connection {}: {}", connection.getId(), response.getType());

        if (response.getType() == MessageType.LOGIN_SUCCESS) {
            sessionManager.registerPlayer(connection, response.getUserId(), response.getUsername());
            if (response.getActiveGame() != null) {
                GameData activeGame = gameRegistry.getGameForPlayer(response.getUserId());
                sessionManager.setInGame(connection.getId());
                gameTimeoutService.onPlayerReconnect(response.getUserId());
                log.info("Connection {} registered for user {} ({}) - rejoining active game {}", connection.getId(), response.getUserId(), response.getUsername(), response.getActiveGame().id());
                if (activeGame != null) {
                    gameService.resendAwaitingInput(activeGame, response.getUserId());
                }
            } else if (response.getActiveDraftId() != null) {
                DraftData activeDraft = draftRegistry.getDraftForPlayer(response.getUserId());
                sessionManager.setInGame(connection.getId());
                log.info("Connection {} registered for user {} ({}) - rejoining active draft {}", connection.getId(), response.getUserId(), response.getUsername(), response.getActiveDraftId());
                if (activeDraft != null) {
                    draftService.resendDraftState(activeDraft, response.getUserId());
                }
            } else {
                log.info("Connection {} registered for user {} ({}) - connection staying open", connection.getId(), response.getUserId(), response.getUsername());
            }
        } else {
            connection.close();
        }
    }

    @Override
    public void handleRegister(Connection connection, RegisterRequest request) throws Exception {
        RegisterResponse response = loginService.register(request);

        connection.sendMessage(objectMapper.writeValueAsString(response));
        log.info("Sent register response to connection {}: {}", connection.getId(), response.getType());

        connection.close();
    }

    @Override
    public void handleCreateGame(Connection connection, CreateGameRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        if (gameRegistry.getGameForPlayer(player.getId()) != null) {
            handleError(connection, "You are already in a game");
            return;
        }

        boolean allRandom = Boolean.TRUE.equals(request.allRandom());
        // Blank means "All sets".
        String randomSet = (request.randomSet() != null && !request.randomSet().isBlank()) ? request.randomSet() : null;
        if (allRandom && randomSet != null && !RandomDeckGenerator.hasDeckableCards(randomSet)) {
            handleError(connection, "The selected set has no cards available for random decks");
            return;
        }

        LobbyService.GameResult result = lobbyService.createGame(request.gameName(), player, request.deckId(),
                allRandom, randomSet);

        // Mark creator as in-game
        sessionManager.setInGame(connection.getId());

        if (Boolean.TRUE.equals(request.vsAi())) {
            GameData gameData = gameRegistry.getGameForPlayer(player.getId());
            String aiDeck = (request.aiDeckId() != null && !request.aiDeckId().isBlank())
                    ? request.aiDeckId() : request.deckId();
            AiDifficulty aiDifficulty = request.aiDifficulty() != null ? request.aiDifficulty() : AiDifficulty.EASY;
            aiPlayerService.joinAsAi(gameData, aiDeck, aiDifficulty);

            // Game is now in MULLIGAN — send full state to the creator
            JoinGame joinGame = gameBroadcastService.getJoinGame(gameData, player.getId());
            sendJoinMessage(connection, MessageType.GAME_JOINED, joinGame);
        } else {
            // Send GAME_JOINED to the creator
            sendJoinMessage(connection, MessageType.GAME_JOINED, result.joinGame());

            // Broadcast NEW_GAME to lobby users only
            broadcastToLobby(MessageType.NEW_GAME, result.lobbyGame());
        }
    }

    @Override
    public void handleJoinGame(Connection connection, JoinGameRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        if (gameRegistry.getGameForPlayer(player.getId()) != null) {
            handleError(connection, "You are already in a game");
            return;
        }

        GameData gameData = gameRegistry.get(request.gameId());
        if (gameData == null) {
            handleError(connection, "Game not found");
            return;
        }

        try {
            LobbyGame lobbyGame = lobbyService.joinGame(gameData, player, request.deckId());

            // Mark joiner as in-game
            sessionManager.setInGame(connection.getId());

            // Send GAME_JOINED to the joiner (with their own hand)
            JoinGame joinerGame = gameBroadcastService.getJoinGame(gameData, player.getId());
            sendJoinMessage(connection, MessageType.GAME_JOINED, joinerGame);

            // Send OPPONENT_JOINED to the creator (with their own hand)
            Connection creatorConnection = sessionManager.getConnectionByUserId(gameData.createdByUserId);
            if (creatorConnection != null && creatorConnection.isOpen()) {
                JoinGame creatorGame = gameBroadcastService.getJoinGame(gameData, gameData.createdByUserId);
                sendJoinMessage(creatorConnection, MessageType.OPPONENT_JOINED, creatorGame);
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.passPriority(gameData, player);
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.keepHand(gameData, player);
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.mulligan(gameData, player);
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.bottomCards(gameData, player, request.cardIndices());
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            playCardRequestDispatchService.dispatch(gameData, player, request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleRevertManaActivations(Connection connection, RevertManaActivationsRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.revertManaActivations(gameData, player);
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.tapPermanent(gameData, player, request.permanentIndex());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleActivateAbility(Connection connection, ActivateAbilityRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.activateAbility(gameData, player, request.permanentIndex(), request.abilityIndex(), request.xValue(), request.targetId(), request.targetZone(), request.targetIds(), request.damageAssignments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleActivateGraveyardAbility(Connection connection, ActivateGraveyardAbilityRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.activateGraveyardAbility(gameData, player, request.graveyardCardIndex(), request.abilityIndex(), request.xValue());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleActivateHandAbility(Connection connection, ActivateHandAbilityRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            if (request.graveyardCardIds() != null && !request.graveyardCardIds().isEmpty()) {
                gameService.activateHandAbilityWithGraveyardTargets(gameData, player, request.handCardIndex(),
                        request.abilityIndex(), request.graveyardCardIds());
            } else {
                gameService.activateHandAbility(gameData, player, request.handCardIndex(), request.abilityIndex(), request.targetId(), request.xValue());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleSacrificePermanent(Connection connection, SacrificePermanentRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.sacrificePermanent(gameData, player, request.permanentIndex(), request.targetId());
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.setAutoStops(gameData, player, request.stops());
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            Map<Integer, UUID> attackTargets = null;
            if (request.attackTargets() != null) {
                attackTargets = new HashMap<>();
                for (var entry : request.attackTargets().entrySet()) {
                    attackTargets.put(entry.getKey(), UUID.fromString(entry.getValue()));
                }
            }
            gameService.declareAttackers(gameData, player, request.attackerIndices(), attackTargets, request.bands());
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

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.declareBlockers(gameData, player, request.blockerAssignments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleInteractionAnswer(Connection connection, InteractionAnswerRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        if (request.shape() == null) {
            handleError(connection, "Missing interaction answer shape");
            return;
        }

        try {
            gameService.handleInteractionAnswer(gameData, player, toAnswer(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    /** Maps the wire answer payload to the engine's {@link InteractionAnswer} by shape. */
    private static InteractionAnswer toAnswer(InteractionAnswerRequest request) {
        return switch (request.shape()) {
            case CARD_INDEX_PICK -> new InteractionAnswer.CardIndexChosen(request.index());
            case GRAVEYARD_INDEX_PICK -> new InteractionAnswer.GraveyardCardChosen(request.index());
            case LIBRARY_INDEX_PICK -> new InteractionAnswer.LibraryCardChosen(request.index());
            case PERMANENT_PICK -> new InteractionAnswer.PermanentChosen(request.id());
            case MULTI_CARD_PICK -> new InteractionAnswer.CardsChosen(request.ids());
            case MULTI_PERMANENT_PICK -> new InteractionAnswer.PermanentsChosen(request.ids());
            case LIST_PICK -> new InteractionAnswer.ListChoiceMade(request.choice());
            case ACCEPT_DECLINE -> new InteractionAnswer.MayAbilityChosen(Boolean.TRUE.equals(request.accepted()));
            case NUMBER_PICK -> new InteractionAnswer.NumberChosen(request.number());
            case SCRY_ORDER -> new InteractionAnswer.ScryOrder(request.order(), request.secondOrder());
            case CARD_ORDER -> new InteractionAnswer.CardOrder(request.order());
            case HAND_TOP_BOTTOM -> new InteractionAnswer.HandTopBottom(request.index(), request.secondIndex());
        };
    }

    @Override
    public void handleCreateDraft(Connection connection, CreateDraftRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        if (gameRegistry.getGameForPlayer(player.getId()) != null) {
            handleError(connection, "You are already in a game");
            return;
        }

        if (draftRegistry.getDraftForPlayer(player.getId()) != null) {
            handleError(connection, "You are already in a draft");
            return;
        }

        try {
            AiDifficulty aiDifficulty = request.aiDifficulty() != null ? request.aiDifficulty() : AiDifficulty.EASY;
            DraftData draftData = draftService.createDraft(
                    request.draftName(), player.getId(), player.getUsername(),
                    request.setCode(), request.aiCount(), aiDifficulty);

            sessionManager.setInGame(connection.getId());

            // Send DRAFT_JOINED to the creator
            draftService.sendDraftJoined(draftData, player.getId());

            // If draft is full (1 human + 7 AI), the first pack is already sent in startDraft()
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleDraftPick(Connection connection, DraftPickRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        DraftData draftData = draftRegistry.getDraftForPlayer(player.getId());
        if (draftData == null) {
            handleError(connection, "Not in a draft");
            return;
        }

        try {
            draftService.handlePick(draftData, player.getId(), request.cardIndex());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleSubmitDeck(Connection connection, SubmitDeckRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        DraftData draftData = draftRegistry.getDraftForPlayer(player.getId());
        if (draftData == null) {
            handleError(connection, "Not in a draft");
            return;
        }

        try {
            draftService.submitDeck(draftData, player.getId(), request.cardIndices(), request.basicLands());
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleCombatDamageAssigned(Connection connection, CombatDamageAssignedRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            // Convert String UUIDs to UUID objects
            java.util.Map<java.util.UUID, Integer> assignments = new java.util.HashMap<>();
            for (var entry : request.damageAssignments().entrySet()) {
                assignments.put(java.util.UUID.fromString(entry.getKey()), entry.getValue());
            }
            gameService.handleCombatDamageAssigned(gameData, player, request.attackerIndex(), assignments);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleRequestCardList(Connection connection, RequestCardListRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        var cards = cardBrowserService.getCardsForSet(request.setCode());
        CardListResponse response = new CardListResponse(request.setCode(), cards);
        connection.sendMessage(objectMapper.writeValueAsString(response));
    }

    @Override
    public void handleSaveDeck(Connection connection, SaveDeckRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        try {
            SaveDeckResponse response = deckService.saveDeck(player.getId(), request);
            connection.sendMessage(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Error saving deck", e);
            handleError(connection, "Failed to save deck: " + e.getMessage());
        }
    }

    @Override
    public void handleValidTargetsRequest(Connection connection, ValidTargetsRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            ValidTargetsResponse response;
            synchronized (gameData) {
                if (request.cardIndex() != null) {
                    // Spell from hand
                    java.util.List<com.github.laxika.magicalvibes.model.Card> hand = gameData.playerHands.get(player.getId());
                    if (hand == null || request.cardIndex() < 0 || request.cardIndex() >= hand.size()) {
                        handleError(connection, "Invalid card index");
                        return;
                    }
                    com.github.laxika.magicalvibes.model.Card card = hand.get(request.cardIndex());
                    response = validTargetService.computeValidTargetsForSpell(
                            gameData, card, player.getId(),
                            request.alreadySelectedIds() != null ? request.alreadySelectedIds() : java.util.List.of(),
                            request.xValue(), request.kicked());
                } else if (request.permanentIndex() != null && request.abilityIndex() != null) {
                    // Activated ability
                    java.util.List<com.github.laxika.magicalvibes.model.Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
                    if (battlefield == null || request.permanentIndex() < 0 || request.permanentIndex() >= battlefield.size()) {
                        handleError(connection, "Invalid permanent index");
                        return;
                    }
                    com.github.laxika.magicalvibes.model.Permanent permanent = battlefield.get(request.permanentIndex());
                    java.util.List<com.github.laxika.magicalvibes.model.ActivatedAbility> abilities = gameService.getEffectiveActivatedAbilities(gameData, permanent);
                    if (request.abilityIndex() < 0 || request.abilityIndex() >= abilities.size()) {
                        handleError(connection, "Invalid ability index");
                        return;
                    }
                    com.github.laxika.magicalvibes.model.ActivatedAbility ability = abilities.get(request.abilityIndex());
                    response = validTargetService.computeValidTargetsForAbility(
                            gameData, permanent.getCard(), ability, player.getId(), request.permanentIndex(),
                            request.alreadySelectedIds() != null ? request.alreadySelectedIds() : java.util.List.of());
                } else {
                    handleError(connection, "Invalid valid targets request");
                    return;
                }
            }
            connection.sendMessage(objectMapper.writeValueAsString(response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handlePaySearchTax(Connection connection, PaySearchTaxRequest request) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.paySearchTax(gameData, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleSurrender(Connection connection) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData == null) {
            handleError(connection, "Not in a game");
            return;
        }

        try {
            gameService.surrender(gameData, player);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleError(connection, e.getMessage());
        }
    }

    @Override
    public void handleLeaveGame(Connection connection) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        GameData gameData = gameRegistry.getGameForPlayer(player.getId());
        if (gameData != null) {
            if (gameData.status == GameStatus.WAITING) {
                // Leaving a WAITING game: cancel it and notify lobby users
                LobbyGame lobbyGame = new LobbyGame(gameData.id, gameData.gameName,
                        gameData.createdByUsername, gameData.playerIds.size(), gameData.status, gameData.allRandom);
                gameRegistry.remove(gameData.id);
                broadcastToLobby(MessageType.GAME_REMOVED, lobbyGame);
            } else if (gameData.status != GameStatus.FINISHED) {
                // Leaving an in-progress game: vs AI → close immediately; human-vs-human → concede.
                sessionManager.clearInGame(connection.getId());
                if (gameTimeoutService.isVsAi(gameData)) {
                    gameTimeoutService.onPlayerDisconnect(player.getId());
                } else {
                    try {
                        gameService.surrender(gameData, player);
                    } catch (IllegalStateException e) {
                        log.warn("Player {} tried to leave already-finished game {}", player.getId(), gameData.id);
                    }
                }

                var games = lobbyService.listRunningGames();
                var response = new com.github.laxika.magicalvibes.networking.message.LobbyGamesResponse(games);
                connection.sendMessage(objectMapper.writeValueAsString(response));
                return;
            }
        }

        // Mark player as back in the lobby so they receive future lobby broadcasts
        sessionManager.clearInGame(connection.getId());

        var games = lobbyService.listRunningGames();
        var response = new com.github.laxika.magicalvibes.networking.message.LobbyGamesResponse(games);
        connection.sendMessage(objectMapper.writeValueAsString(response));
    }

    @Override
    public void handleLeaveDraft(Connection connection) throws Exception {
        Player player = sessionManager.getPlayer(connection.getId());
        if (player == null) {
            handleError(connection, "Not authenticated");
            return;
        }

        DraftData draftData = draftRegistry.getDraftForPlayer(player.getId());
        if (draftData != null) {
            // Only allow leaving if the draft tournament is in progress or finished
            if (draftData.status == DraftStatus.TOURNAMENT || draftData.status == DraftStatus.FINISHED) {
                draftData.playerIds.remove(player.getId());

                // If no human players remain, clean up the draft
                boolean hasHumanPlayers = draftData.playerIds.stream()
                        .anyMatch(id -> !draftData.aiPlayerIds.contains(id));
                if (!hasHumanPlayers) {
                    draftRegistry.remove(draftData.id);
                }
            }
        }

        // Mark player as back in the lobby
        sessionManager.clearInGame(connection.getId());

        var games = lobbyService.listRunningGames();
        var response = new com.github.laxika.magicalvibes.networking.message.LobbyGamesResponse(games);
        connection.sendMessage(objectMapper.writeValueAsString(response));
    }

    @Override
    public void handleError(Connection connection, String message) throws Exception {
        if (!connection.isOpen()) {
            log.debug("Suppressed error for closed connection {}: {}", connection.getId(), message);
            return;
        }
        log.warn("Sending error to {}: {}", connection.getId(), message);
        ErrorMessage error = new ErrorMessage(message);
        connection.sendMessage(objectMapper.writeValueAsString(error));
    }

    @Override
    public void handleConnectionClosed(UUID playerId) {
        gameTimeoutService.onPlayerDisconnect(playerId);
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

