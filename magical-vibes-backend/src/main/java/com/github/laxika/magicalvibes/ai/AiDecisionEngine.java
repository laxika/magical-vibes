package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
    protected final CombatAttackService combatAttackService;
    protected final GameBroadcastService gameBroadcastService;

    protected final AiManaManager manaManager;
    protected final AiTargetSelector targetSelector;
    protected final AiChoiceHandler choiceHandler;

    protected Connection selfConnection;

    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                            MessageHandler messageHandler, GameQueryService gameQueryService,
                            CombatAttackService combatAttackService,
                            GameBroadcastService gameBroadcastService,
                            TargetValidationService targetValidationService) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameRegistry = gameRegistry;
        this.messageHandler = messageHandler;
        this.gameQueryService = gameQueryService;
        this.combatAttackService = combatAttackService;
        this.gameBroadcastService = gameBroadcastService;

        this.manaManager = new AiManaManager(gameQueryService);
        this.targetSelector = new AiTargetSelector(gameQueryService, targetValidationService);
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
            case "CHOOSE_FROM_LIST" -> choiceHandler.handleColorChoice(gameData);
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
                int handSizeBefore = hand.size();
                final int idx = i;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(idx, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
                // Verify the land was actually played — handlePlayCard silently
                // swallows errors, so we must confirm the state actually changed.
                if (hand.size() >= handSizeBefore) {
                    log.warn("AI: Land play failed silently in game {}", gameId);
                    return false;
                }
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

    /**
     * Merges must-attack indices into an attacker list, ensuring all creatures
     * with "attacks each combat if able" are included.
     */
    protected List<Integer> enforceMustAttack(List<Integer> attackerIndices, List<Integer> mustAttackIndices) {
        if (mustAttackIndices.isEmpty()) return attackerIndices;
        LinkedHashSet<Integer> merged = new LinkedHashSet<>(attackerIndices);
        merged.addAll(mustAttackIndices);
        return new ArrayList<>(merged);
    }

    /**
     * Sends a blocker declaration with automatic fallback to empty blockers
     * if the original declaration fails server-side validation.
     */
    protected void sendBlockerDeclaration(GameData gameData, DeclareBlockersRequest request) {
        try {
            messageHandler.handleDeclareBlockers(selfConnection, request);
        } catch (Exception e) {
            log.warn("AI: Blocker declaration rejected in game {}: {}. Falling back to no blockers.", gameId, e.getMessage());
            try {
                messageHandler.handleDeclareBlockers(selfConnection, new DeclareBlockersRequest(List.of()));
            } catch (Exception e2) {
                log.error("AI: Empty blocker declaration also failed in game {}", gameId, e2);
            }
        }
    }

    /**
     * Returns true if the card can be cast considering mana affordability (with cost
     * modifiers), non-mana restrictions (spell limit, type restrictions, etc.),
     * sacrifice costs, and graveyard requirements.
     */
    protected boolean isSpellCastable(GameData gameData, Card card, ManaPool virtualPool) {
        if (!gameBroadcastService.isSpellCastingAllowed(gameData, aiPlayer.getId(), card)) {
            return false;
        }
        if (!canPaySacrificeCosts(gameData, card)) {
            return false;
        }
        // For X spells that exile creatures from graveyard, ensure at least 1 creature exists
        ManaCost cost = new ManaCost(card.getManaCost());
        if (cost.hasX() && getMaxXForGraveyardRequirements(gameData, card) <= 0) {
            return false;
        }
        return canAffordSpell(gameData, card, virtualPool);
    }

    /**
     * Checks if the card's mana cost (including cost modifiers from battlefield effects)
     * can be paid from the given mana pool.
     */
    protected boolean canAffordSpell(GameData gameData, Card card, ManaPool virtualPool) {
        ManaCost cost = new ManaCost(card.getManaCost());
        int modifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card);
        if (cost.hasX()) {
            if (!cost.canPay(virtualPool, Math.max(0, 1 + modifier))) return false;
        } else {
            if (!cost.canPay(virtualPool, modifier)) return false;
        }
        if (card.isRequiresCreatureMana() && !cost.canPayCreatureOnly(virtualPool, modifier)) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the player's battlefield can satisfy all sacrifice costs
     * in the card's SPELL effects (e.g. SacrificeArtifactCost, SacrificeCreatureCost,
     * SacrificePermanentCost). Returns false if any sacrifice cost cannot be paid.
     */
    protected boolean canPaySacrificeCosts(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeArtifactCost) {
                boolean hasArtifact = battlefield.stream()
                        .anyMatch(p -> gameQueryService.isArtifact(gameData, p));
                if (!hasArtifact) return false;
            } else if (effect instanceof SacrificeCreatureCost) {
                boolean hasCreature = battlefield.stream()
                        .anyMatch(p -> gameQueryService.isCreature(gameData, p));
                if (!hasCreature) return false;
            } else if (effect instanceof SacrificePermanentCost sacCost) {
                boolean hasMatch = battlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacCost.filter()));
                if (!hasMatch) return false;
            }
        }
        return true;
    }

    /**
     * Returns the maximum X value allowed by graveyard creature requirements.
     * For cards with {@link ExileCreaturesFromGraveyardAndCreateTokensEffect},
     * X cannot exceed the number of creature cards in the caster's graveyard.
     * Returns {@link Integer#MAX_VALUE} if the card has no such requirement.
     */
    protected int getMaxXForGraveyardRequirements(GameData gameData, Card card) {
        boolean needsGraveyardCreatures = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(ExileCreaturesFromGraveyardAndCreateTokensEffect.class::isInstance);
        if (!needsGraveyardCreatures) {
            return Integer.MAX_VALUE;
        }
        return (int) gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of()).stream()
                .filter(c -> c.hasType(CardType.CREATURE))
                .count();
    }

    /**
     * Selects a permanent to sacrifice for the card's sacrifice cost, if any.
     * Picks the weakest creature (lowest effective power + toughness) for creature
     * sacrifice costs, or the first matching permanent for other sacrifice types.
     * Returns null if the card has no sacrifice cost.
     */
    protected UUID selectSacrificeTarget(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeCreatureCost) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .min(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)
                                + gameQueryService.getEffectiveToughness(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            } else if (effect instanceof SacrificeArtifactCost) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.isArtifact(gameData, p))
                        .findFirst()
                        .map(Permanent::getId)
                        .orElse(null);
            } else if (effect instanceof SacrificePermanentCost sacCost) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacCost.filter()))
                        .findFirst()
                        .map(Permanent::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    protected IntConsumer tapPermanentAction() {
        return idx -> send(() -> messageHandler.handleTapPermanent(selfConnection, new TapPermanentRequest(idx)));
    }

    protected void send(MessageHandlerAction action) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return;
        }
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
