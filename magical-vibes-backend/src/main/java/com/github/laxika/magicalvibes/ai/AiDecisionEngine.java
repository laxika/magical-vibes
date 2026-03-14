package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Abstract base class for all AI difficulty levels. Provides message dispatch,
 * mulligan handling, land playing, and shared utility. Delegates interactive
 * choices to {@link AiChoiceHandler}, mana management to {@link AiManaManager},
 * and targeting to {@link AiTargetSelector}.
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #handleGameState} — priority and spell-casting strategy</li>
 *   <li>{@link #handleAttackers} — attacker declaration strategy</li>
 *   <li>{@link #handleBlockers} — blocker declaration strategy</li>
 * </ul>
 */
@Slf4j
public abstract class AiDecisionEngine {

    protected final UUID gameId;
    protected final Player aiPlayer;
    protected final GameRegistry gameRegistry;
    protected final MessageHandler messageHandler;
    protected final GameQueryService gameQueryService;

    protected final AiManaManager manaManager;
    protected final AiTargetSelector targetSelector;
    protected final AiChoiceHandler choiceHandler;

    protected Connection selfConnection;

    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                            MessageHandler messageHandler, GameQueryService gameQueryService) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameRegistry = gameRegistry;
        this.messageHandler = messageHandler;
        this.gameQueryService = gameQueryService;

        this.manaManager = new AiManaManager(gameQueryService);
        this.targetSelector = new AiTargetSelector(gameQueryService);
        this.choiceHandler = new AiChoiceHandler(gameId, aiPlayer.getId(), gameQueryService, messageHandler);
    }

    public void setSelfConnection(Connection selfConnection) {
        this.selfConnection = selfConnection;
        this.choiceHandler.setSelfConnection(selfConnection);
    }

    // ===== Message Dispatch =====

    public void handleMessage(String type, String json) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return;
        }

        switch (type) {
            case "GAME_STATE" -> handleGameState(gameData);
            case "MULLIGAN_RESOLVED" -> handleMulliganResolved(gameData);
            case "SELECT_CARDS_TO_BOTTOM" -> choiceHandler.handleBottomCards(gameData);
            case "AVAILABLE_ATTACKERS" -> handleAttackers(gameData);
            case "AVAILABLE_BLOCKERS" -> handleBlockers(gameData);
            case "CHOOSE_CARD_FROM_HAND" -> handleCardChoice(gameData);
            case "CHOOSE_PERMANENT" -> choiceHandler.handlePermanentChoice(gameData);
            case "CHOOSE_MULTIPLE_PERMANENTS" -> choiceHandler.handleMultiPermanentChoice(gameData);
            case "CHOOSE_COLOR" -> choiceHandler.handleColorChoice(gameData);
            case "MAY_ABILITY_CHOICE" -> choiceHandler.handleMayAbilityChoice(gameData);
            case "X_VALUE_CHOICE" -> choiceHandler.handleXValueChoice(gameData);
            case "SCRY" -> choiceHandler.handleScry(gameData);
            case "REORDER_LIBRARY_CARDS" -> choiceHandler.handleReorderCards(gameData);
            case "CHOOSE_CARD_FROM_LIBRARY" -> choiceHandler.handleLibrarySearch(gameData);
            case "CHOOSE_CARD_FROM_GRAVEYARD" -> choiceHandler.handleGraveyardChoice(gameData);
            case "CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS" -> choiceHandler.handleMultiGraveyardChoice(gameData);
            case "CHOOSE_HAND_TOP_BOTTOM" -> choiceHandler.handleHandTopBottom(gameData);
            case "CHOOSE_FROM_REVEALED_HAND" -> choiceHandler.handleRevealedHandChoice(gameData);
            case "COMBAT_DAMAGE_ASSIGNMENT" -> choiceHandler.handleCombatDamageAssignment(gameData);
            case "GAME_OVER" -> log.info("AI: Game {} is over", gameId);
            default -> {
                // Ignore informational messages (BATTLEFIELD_UPDATED, MANA_UPDATED, etc.)
            }
        }
    }

    // ===== Abstract Methods =====

    protected abstract void handleGameState(GameData gameData);

    protected abstract void handleAttackers(GameData gameData);

    protected abstract void handleBlockers(GameData gameData);

    // ===== Card Choice (overridable by Medium AI) =====

    protected void handleCardChoice(GameData gameData) {
        choiceHandler.handleCardChoice(gameData);
    }

    // ===== Mulligan =====

    public void handleInitialMulligan() {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null) return;
        if (shouldKeepHand(gameData)) {
            log.info("AI: Keeping hand in game {}", gameId);
            send(() -> messageHandler.handleKeepHand(selfConnection, new KeepHandRequest()));
        } else {
            log.info("AI: Taking mulligan in game {}", gameId);
            send(() -> messageHandler.handleMulligan(selfConnection, new MulliganRequest()));
        }
    }

    private void handleMulliganResolved(GameData gameData) {
        if (gameData.playerKeptHand.contains(aiPlayer.getId())) {
            return;
        }
        handleInitialMulligan();
    }

    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) {
            return true;
        }

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) {
            return true;
        }

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        if (mulliganCount >= 2) {
            return landCount >= 1;
        }
        if (mulliganCount >= 1) {
            return landCount >= 1 && landCount <= 5;
        }

        return landCount >= 2 && landCount <= 5;
    }

    // ===== Land Playing =====

    protected boolean tryPlayLand(GameData gameData) {
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(aiPlayer.getId(), 0);
        if (landsPlayed > 0) {
            return false;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) {
                log.info("AI: Playing land {} in game {}", card.getName(), gameId);
                final int idx = i;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(idx, null, null, null, null, null, null, null, null, null, null, null)));
                return true;
            }
        }
        return false;
    }

    // ===== Utility =====

    protected boolean hasPriority(GameData gameData) {
        if (gameData.status != GameStatus.RUNNING) {
            return false;
        }
        synchronized (gameData) {
            if (gameData.interaction.isAwaitingInput()) {
                return false;
            }
            UUID priorityHolder = getPriorityPlayerId(gameData);
            return priorityHolder != null && priorityHolder.equals(aiPlayer.getId());
        }
    }

    protected UUID getPriorityPlayerId(GameData gameData) {
        if (gameData.activePlayerId == null) {
            return null;
        }
        if (!gameData.priorityPassedBy.contains(gameData.activePlayerId)) {
            return gameData.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(gameData.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!gameData.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    protected IntConsumer tapPermanentAction() {
        return idx -> send(() -> messageHandler.handleTapPermanent(selfConnection, new TapPermanentRequest(idx)));
    }

    protected void send(MessageHandlerAction action) {
        try {
            action.execute();
        } catch (Exception e) {
            log.error("AI: Error sending message in game {}", gameId, e);
        }
    }

    @FunctionalInterface
    protected interface MessageHandlerAction {
        void execute() throws Exception;
    }
}
