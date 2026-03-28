package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Medium difficulty AI that uses board evaluation, spell evaluation, and exhaustive
 * combat search to make smarter decisions than the base AiDecisionEngine.
 */
@Slf4j
public class MediumAiDecisionEngine extends AiDecisionEngine {

    private final BoardEvaluator boardEvaluator;
    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;

    public MediumAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                  MessageHandler messageHandler, GameQueryService gameQueryService,
                                  CombatAttackService combatAttackService,
                                  GameBroadcastService gameBroadcastService,
                                  TargetValidationService targetValidationService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService);
        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
    }

    @Override
    protected void handleGameState(GameData gameData) {
        if (!hasPriority(gameData)) {
            return;
        }

        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean isActivePlayer = aiPlayer.getId().equals(gameData.activePlayerId);

        if (isMainPhase && isActivePlayer && gameData.stack.isEmpty()) {
            if (tryPlayLand(gameData)) {
                return;
            }

            if (tryCastSpell(gameData)) {
                return;
            }
        }

        // Try casting instants based on timing heuristics
        if (tryCastInstantWithTiming(gameData)) {
            return;
        }

        // Pass priority
        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    protected boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Evaluate all castable spells using SpellEvaluator
        record CastCandidate(int index, double value) {}
        List<CastCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) continue;
            if (card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;

            if (!isSpellCastable(gameData, card, virtualPool)) {
                continue;
            }

            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                candidates.add(new CastCandidate(i, value));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        // Cast the highest-value spell
        candidates.sort(Comparator.comparingDouble(CastCandidate::value).reversed());
        CastCandidate best = candidates.getFirst();
        Card card = hand.get(best.index);

        // Handle modal spells (ChooseOneEffect)
        ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
        if (modalPlan == null && findChooseOneEffect(card) != null) {
            return false;
        }

        // Build damage assignments for divided damage spells
        Map<UUID, Integer> damageAssignments = null;
        if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
            damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
            if (damageAssignments == null) {
                return false;
            }
        }

        // Determine target if needed (skip for modal and damage distribution spells)
        UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
        List<UUID> multiTargetIds = null;
        boolean isMultiTarget = card.getSpellTargets().size() > 1;
        if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) {
                return false;
            }
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        // Select sacrifice target if the spell has a sacrifice cost
        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        // Select graveyard cards to exile if the spell has a graveyard exile cost
        List<Integer> exileGraveyardCardIndices = null;
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        // Calculate X value (for modal spells, xValue is the mode index)
        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card);
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                if (maxX <= 0) {
                    return false;
                }
                List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                        gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) {
                    return false;
                }
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = chosen.getCard().getManaValue();
            } else {
                int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool, costModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                if (smartX <= 0) {
                    return false;
                }
                xValue = smartX;
            }
        }

        log.info("AI (Medium): Casting {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", best.value), gameId);
        tapManaForSpell(gameData, card, xValue);
        int handSizeBefore = hand.size();
        final UUID finalTargetId = targetId;
        final int cardIndex = best.index;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, null, finalExileGraveyardCardIndices, null, null, null)));
        // Verify the spell was actually cast — handlePlayCard silently
        // swallows errors, so we must confirm the state actually changed.
        if (hand.size() >= handSizeBefore) {
            Card failedCard = hand.size() > cardIndex ? hand.get(cardIndex) : null;
            ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
            log.warn("AI (Medium): PlayCard failed silently in game {}. Card='{}' index={} step={} isActive={} stackEmpty={} pool={} priorityPassed={}",
                    gameId, failedCard != null ? failedCard.getName() : "?", cardIndex,
                    gameData.currentStep, aiPlayer.getId().equals(gameData.activePlayerId),
                    gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                    gameData.priorityPassedBy);
            return false;
        }
        return true;
    }

    // ===== Instant Casting with Timing Heuristics =====

    /**
     * Tries to cast the best instant at the right timing. Classifies each instant
     * by category and only casts when the current game state matches the ideal window.
     * Falls back to casting any instant during main phase if no sorceries were cast.
     */
    private boolean tryCastInstantWithTiming(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        boolean isOpponentsTurn = !aiPlayer.getId().equals(gameData.activePlayerId);
        TurnStep step = gameData.currentStep;

        record CastCandidate(int index, double value) {}
        List<CastCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (EffectResolution.needsSpellTarget(card)) continue; // Can't target spells on stack
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            InstantCategory category = InstantCategoryClassifier.classify(card);
            if (!isGoodTiming(category, step, isOpponentsTurn)) continue;

            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                candidates.add(new CastCandidate(i, value));
            }
        }

        if (candidates.isEmpty()) return false;

        candidates.sort(Comparator.comparingDouble(CastCandidate::value).reversed());
        CastCandidate best = candidates.getFirst();
        return castInstantAtIndex(gameData, hand, best.index, best.value);
    }

    /**
     * Determines whether the current game state is a good time to cast an instant
     * of the given category.
     */
    private boolean isGoodTiming(InstantCategory category, TurnStep step, boolean isOpponentsTurn) {
        return switch (category) {
            case REMOVAL -> isOpponentsTurn
                    && (step == TurnStep.BEGINNING_OF_COMBAT
                    || step == TurnStep.DECLARE_ATTACKERS
                    || step == TurnStep.DECLARE_BLOCKERS);
            case BURN_TO_FACE -> isOpponentsTurn && step == TurnStep.END_STEP;
            case CARD_ADVANTAGE -> isOpponentsTurn && step == TurnStep.END_STEP;
            case COMBAT_TRICK -> !isOpponentsTurn
                    && (step == TurnStep.DECLARE_BLOCKERS || step == TurnStep.COMBAT_DAMAGE);
            case COUNTERSPELL -> false; // AI can't target spells on the stack yet
            case OTHER -> step == TurnStep.PRECOMBAT_MAIN || step == TurnStep.POSTCOMBAT_MAIN
                    || (isOpponentsTurn && step == TurnStep.END_STEP);
        };
    }

    /**
     * Casts the instant at the given hand index. Shared by timing-based and
     * fallback instant casting paths.
     */
    private boolean castInstantAtIndex(GameData gameData, List<Card> hand, int cardIndex, double value) {
        Card card = hand.get(cardIndex);
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Handle modal spells (ChooseOneEffect)
        ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
        if (modalPlan == null && findChooseOneEffect(card) != null) {
            return false;
        }

        Map<UUID, Integer> damageAssignments = null;
        if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
            damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
            if (damageAssignments == null) return false;
        }

        UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
        List<UUID> multiTargetIds = null;
        boolean isMultiTarget = card.getSpellTargets().size() > 1;
        if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) return false;
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) return false;
        }

        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        List<Integer> exileGraveyardCardIndices = null;
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int instantCostModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card);
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, instantCostModifier);
                if (maxX <= 0) return false;
                List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                        gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) return false;
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = chosen.getCard().getManaValue();
            } else {
                int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool, instantCostModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                if (smartX <= 0) return false;
                xValue = smartX;
            }
        }

        log.info("AI (Medium): Casting instant {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", value), gameId);
        tapManaForSpell(gameData, card, xValue);
        int handSizeBefore = hand.size();
        final UUID finalTargetId = targetId;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, null, finalExileGraveyardCardIndices, null, null, null)));
        if (hand.size() >= handSizeBefore) {
            log.warn("AI (Medium): Instant cast failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);

        List<Integer> attackerIndices = combatSimulator.findBestAttackers(
                gameData, aiPlayer.getId(), availableIndices, mustAttackIndices);

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

        log.info("AI (Medium): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
        final List<Integer> finalAttackerIndices = attackerIndices;
        send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                new DeclareAttackersRequest(finalAttackerIndices, null)));
    }

    @Override
    protected void handleBlockers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null) {
            send(() -> messageHandler.handleDeclareBlockers(selfConnection,
                    new DeclareBlockersRequest(List.of())));
            return;
        }

        // Find attacker indices
        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                attackerIndices.add(i);
            }
        }

        // Find available blocker indices
        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gameData, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }

        List<int[]> assignments = combatSimulator.findBestBlockers(
                gameData, aiPlayer.getId(), attackerIndices, blockerIndices);

        List<BlockerAssignment> blockerAssignments = assignments.stream()
                .map(a -> new BlockerAssignment(a[0], a[1]))
                .toList();

        log.info("AI (Medium): Declaring {} blockers in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
    }

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) return;

        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        // Discard the card with the lowest spell value instead of highest mana cost
        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.estimateSpellValue(gameData, hand.get(i), aiPlayer.getId())))
                .orElse(validIndices.iterator().next());

        log.info("AI (Medium): Discarding card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return true;

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) return true;

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        // Basic land check first
        if (landCount == 0 && mulliganCount < 2) return false;
        if (landCount > 5) return false;

        // Score hand by counting playable spells in turns 1-3
        double handScore = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                handScore += 1.5; // Lands have base value
                continue;
            }
            int mv = card.getManaValue();
            if (mv <= landCount + 1) {
                // Playable in first few turns
                handScore += 3.0;
            } else if (mv <= landCount + 3) {
                // Playable soon
                handScore += 1.5;
            } else {
                // Expensive, low value early
                handScore += 0.5;
            }
        }

        // Threshold scales with mulligan count (more lenient as we mulligan more)
        double threshold = 12.0 - mulliganCount * 3.0;
        return handScore >= threshold;
    }

}
