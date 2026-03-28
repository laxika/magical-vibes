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
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
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
            case "CHOOSE_MULTIPLE_CARDS" -> choiceHandler.handleMultiCardChoice(gameData);
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
                        new PlayCardRequest(idx, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
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
     * Ensures the attacker list is non-empty when an opponent's effect forces
     * the player to attack with at least one creature (e.g. Trove of Temptation).
     * If the list is empty and the player is forced, picks the first available attacker.
     */
    protected List<Integer> enforceMustAttackWithAtLeastOne(GameData gameData, List<Integer> attackerIndices,
                                                            List<Integer> availableIndices) {
        if (!attackerIndices.isEmpty() || availableIndices.isEmpty()) return attackerIndices;
        if (!combatAttackService.isOpponentForcedToAttack(gameData, aiPlayer.getId())) return attackerIndices;
        List<Integer> forced = new ArrayList<>(attackerIndices);
        forced.add(availableIndices.getFirst());
        return forced;
    }

    /**
     * Returns the maximum number of attackers the AI can afford given the current
     * attack tax (e.g. Windborn Muse / Ghostly Prison). Returns {@link Integer#MAX_VALUE}
     * if there is no attack tax.
     */
    protected int getMaxAffordableAttackers(GameData gameData) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, aiPlayer.getId());
        if (taxPerCreature <= 0) {
            return Integer.MAX_VALUE;
        }
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        return virtualPool.getTotal() / taxPerCreature;
    }

    /**
     * Caps the attacker list to the maximum affordable given the attack tax,
     * then taps lands to pay the tax before the declaration is sent.
     */
    protected List<Integer> prepareAttackersForTax(GameData gameData, List<Integer> attackerIndices) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, aiPlayer.getId());
        if (taxPerCreature <= 0 || attackerIndices.isEmpty()) {
            return attackerIndices;
        }
        int maxAffordable = getMaxAffordableAttackers(gameData);
        if (maxAffordable <= 0) {
            return List.of();
        }
        List<Integer> capped = attackerIndices.size() <= maxAffordable
                ? attackerIndices
                : new ArrayList<>(attackerIndices.subList(0, maxAffordable));
        // Tap lands to put enough mana in the pool to pay the tax
        int totalTax = taxPerCreature * capped.size();
        String taxCostStr = "{" + totalTax + "}";
        manaManager.tapLandsForCost(gameData, aiPlayer.getId(), taxCostStr, 0, manaTapAction());
        return capped;
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
        if (!canPayGraveyardExileCosts(gameData, card)) {
            return false;
        }
        // For X spells that exile creatures from graveyard, ensure at least 1 creature exists
        ManaCost cost = new ManaCost(card.getManaCost());
        if (cost.hasX() && getMaxXForGraveyardRequirements(gameData, card) <= 0) {
            return false;
        }
        if (!canAffordSpell(gameData, card, virtualPool)) {
            return false;
        }
        // For modal spells, ensure at least one mode has valid targets
        if (!hasValidModalMode(gameData, card)) {
            return false;
        }
        return true;
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
     * Checks whether the player's graveyard can satisfy all graveyard exile costs
     * in the card's SPELL effects (e.g. ExileNCardsFromGraveyardCost, ExileCardFromGraveyardCost).
     * Returns false if any graveyard exile cost cannot be paid.
     */
    protected boolean canPayGraveyardExileCosts(GameData gameData, Card card) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileNCardsFromGraveyardCost cost) {
                long matchingCount = graveyard.stream()
                        .filter(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))
                        .count();
                if (matchingCount < cost.count()) return false;
            } else if (effect instanceof ExileCardFromGraveyardCost cost) {
                boolean hasMatch = graveyard.stream()
                        .anyMatch(c -> cost.requiredType() == null || c.hasType(cost.requiredType()));
                if (!hasMatch) return false;
            } else if (effect instanceof ExileXCardsFromGraveyardCost) {
                if (graveyard.isEmpty()) return false;
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
     * Returns true if the card's target filter contains a {@link PermanentManaValueEqualsXPredicate},
     * meaning the target's mana value must match X (e.g. Entrancing Melody).
     */
    protected boolean hasPermanentManaValueEqualsXTarget(Card card) {
        TargetFilter filter = card.getTargetFilter();
        if (filter instanceof PermanentPredicateTargetFilter pf) {
            return containsManaValueEqualsXPredicate(pf.predicate());
        }
        return false;
    }

    private boolean containsManaValueEqualsXPredicate(
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate predicate) {
        if (predicate instanceof PermanentManaValueEqualsXPredicate) {
            return true;
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(this::containsManaValueEqualsXPredicate);
        }
        return false;
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

    /**
     * Finds an ExileXCardsFromGraveyardCost in the card's SPELL effects, if any.
     */
    protected ExileXCardsFromGraveyardCost findExileXGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileXCardsFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Finds an ExileNCardsFromGraveyardCost in the card's SPELL effects, if any.
     */
    protected ExileNCardsFromGraveyardCost findExileNGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileNCardsFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Returns indices for all cards in the player's graveyard, for use with
     * {@link ExileXCardsFromGraveyardCost}. Returns an empty list if the graveyard is empty.
     */
    protected List<Integer> selectAllGraveyardIndices(GameData gameData) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    /**
     * Selects exactly N graveyard card indices matching the required type for
     * {@link ExileNCardsFromGraveyardCost} (e.g. Skaab Ruinator's "exile 3 creature cards").
     * Returns null if the graveyard doesn't have enough matching cards.
     */
    protected List<Integer> selectNGraveyardIndicesToExile(GameData gameData, ExileNCardsFromGraveyardCost cost) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card c = graveyard.get(i);
            if (cost.requiredType() == null || c.hasType(cost.requiredType())) {
                matchingIndices.add(i);
            }
        }
        if (matchingIndices.size() < cost.count()) {
            return null;
        }
        return new ArrayList<>(matchingIndices.subList(0, cost.count()));
    }

    // ===== Modal Spell Handling (ChooseOneEffect) =====

    /**
     * Internal record for modal spell casting: holds the selected mode index
     * (used as xValue in PlayCardRequest) and the target for that mode.
     */
    protected record ModalCastPlan(int modeIndex, UUID targetId) {}

    /**
     * Finds the ChooseOneEffect in the card's SPELL effects, if any.
     */
    protected ChooseOneEffect findChooseOneEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ChooseOneEffect coe) {
                return coe;
            }
        }
        return null;
    }

    /**
     * Returns true if the card is non-modal, or if at least one modal mode
     * has valid targets available (excluding spell-targeting modes the AI can't handle).
     */
    protected boolean hasValidModalMode(GameData gameData, Card card) {
        ChooseOneEffect coe = findChooseOneEffect(card);
        if (coe == null) return true;

        for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
            if (isModalModeValid(gameData, card, option)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For a ChooseOneEffect card, selects the first valid non-spell mode and
     * finds a target if needed. Returns null if the card is not modal or
     * if no valid mode exists.
     */
    protected ModalCastPlan prepareModalSpellCast(GameData gameData, Card card) {
        ChooseOneEffect coe = findChooseOneEffect(card);
        if (coe == null) return null;

        for (int i = 0; i < coe.options().size(); i++) {
            ChooseOneEffect.ChooseOneOption option = coe.options().get(i);
            CardEffect effect = option.effect();

            if (effect.canTargetSpell()) continue;

            if (effect.canTargetPermanent()) {
                UUID target = findModalPermanentTarget(gameData, card, option);
                if (target != null) return new ModalCastPlan(i, target);
                continue;
            }

            if (effect.canTargetPlayer()) {
                UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
                return new ModalCastPlan(i, opponentId);
            }

            if (effect.canTargetGraveyard()) {
                List<Card> targets = targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId());
                if (!targets.isEmpty()) return new ModalCastPlan(i, targets.getFirst().getId());
                continue;
            }

            // No targeting required — mode is always valid
            return new ModalCastPlan(i, null);
        }
        return null;
    }

    private boolean isModalModeValid(GameData gameData, Card card, ChooseOneEffect.ChooseOneOption option) {
        CardEffect effect = option.effect();
        if (effect.canTargetSpell()) return false;
        if (effect.canTargetPermanent()) return findModalPermanentTarget(gameData, card, option) != null;
        if (effect.canTargetPlayer()) return true;
        if (effect.canTargetGraveyard()) {
            return !targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId()).isEmpty();
        }
        return true;
    }

    private UUID findModalPermanentTarget(GameData gameData, Card card, ChooseOneEffect.ChooseOneOption option) {
        var savedFilter = card.getCastTimeTargetFilter();
        card.setCastTimeTargetFilter(option.targetFilter());
        try {
            UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
            for (UUID playerId : new UUID[]{opponentId, aiPlayer.getId()}) {
                if (playerId == null) continue;
                for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (targetSelector.isValidPermanentTarget(gameData, card, p, aiPlayer.getId())) {
                        return p.getId();
                    }
                }
            }
            return null;
        } finally {
            card.setCastTimeTargetFilter(savedFilter);
        }
    }

    /**
     * Taps lands (and creature-mana producers if needed) to pay for the given spell
     * before sending a PlayCardRequest. Must be called before handlePlayCard so the
     * actual mana pool satisfies the playability check in SpellCastingService.
     */
    protected void tapManaForSpell(GameData gameData, Card card, Integer xValue) {
        if (card.getManaCost() == null) return;
        int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card);
        AiManaManager.ManaTapAction tap = manaTapAction();

        if (card.isRequiresCreatureMana()) {
            manaManager.tapCreaturesForCost(gameData, aiPlayer.getId(), card.getManaCost(), costModifier, tap);
            return;
        }

        ManaCost cost = new ManaCost(card.getManaCost());
        if (cost.hasX() && xValue != null) {
            manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, xValue, costModifier, tap);
        } else {
            manaManager.tapLandsForCost(gameData, aiPlayer.getId(), card.getManaCost(), costModifier, tap);
        }
    }

    protected AiManaManager.ManaTapAction manaTapAction() {
        return (idx, abilityIndex) -> {
            if (abilityIndex != null) {
                send(() -> messageHandler.handleActivateAbility(selfConnection,
                        new ActivateAbilityRequest(idx, abilityIndex, null, null, null, null, null)));
            } else {
                send(() -> messageHandler.handleTapPermanent(selfConnection, new TapPermanentRequest(idx)));
            }
        };
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
