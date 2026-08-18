package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
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
                                  GameService gameService, GameQueryService gameQueryService,
                                  BlockLegalityService blockLegalityService,
                                  CombatAttackService combatAttackService,
                                  GameActionAvailabilityService actionAvailabilityService,
                                  CastingCostService castingCostService,
                                  CastingPermissionService castingPermissionService,
                                  TargetValidationService targetValidationService,
                                  TargetLegalityService targetLegalityService) {
        this(gameId, aiPlayer, gameRegistry,
                new AiGameActions(gameId, aiPlayer, gameService, gameRegistry),
                gameQueryService, blockLegalityService, combatAttackService,
                actionAvailabilityService, castingCostService, castingPermissionService,
                targetValidationService, targetLegalityService);
    }

    public MediumAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                  AiGameActions gameActions, GameQueryService gameQueryService,
                                  BlockLegalityService blockLegalityService,
                                  CombatAttackService combatAttackService,
                                  GameActionAvailabilityService actionAvailabilityService,
                                  CastingCostService castingCostService,
                                  CastingPermissionService castingPermissionService,
                                  TargetValidationService targetValidationService,
                                  TargetLegalityService targetLegalityService) {
        super(gameId, aiPlayer, gameRegistry, gameActions, gameQueryService, blockLegalityService, combatAttackService, actionAvailabilityService, castingCostService, castingPermissionService, targetValidationService, targetLegalityService);
        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, blockLegalityService, boardEvaluator);
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
        send(() -> gameActions.handlePassPriority(new PassPriorityRequest()));
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
        List<UUID> multiTargetIds = modalPlan != null ? modalPlan.targetIds() : null;
        boolean isMultiTarget = targetSelector.needsMultiTargetSelection(card);
        if (modalPlan == null && !EffectResolution.needsDamageDistribution(card)
                && targetSelector.hasSeparateGraveyardTarget(card)) {
            AiTargetSelector.SpellTargetSelection selection = targetSelector.chooseSeparateGraveyardTargets(
                    gameData, card, aiPlayer.getId());
            if (selection == null) {
                return false;
            }
            targetId = selection.targetId();
            multiTargetIds = selection.targetIds();
        } else if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) {
                return false;
            }
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card)
                && (EffectResolution.needsTarget(card) || card.isAura())
                && !hasPermanentManaValueEqualsXTarget(card)
                && !hasPermanentManaValueAtMostXTarget(card)) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        // Check targeting tax (e.g. Kopala, Warden of Waves)
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
        }

        // Select sacrifice target if the spell has a sacrifice cost
        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        // Select graveyard cards to exile if the spell has a graveyard exile cost
        List<Integer> exileGraveyardCardIndices = null;
        ExileXCardsFromGraveyardCost exileXCost = findExileXGraveyardCost(card);
        if (exileXCost != null) {
            exileGraveyardCardIndices = selectExileXGraveyardIndices(gameData, exileXCost);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        // Calculate X value (for modal spells, xValue is the mode index)
        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card) || hasPermanentManaValueAtMostXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                maxX = manaManager.clampByXValueCap(gameData, aiPlayer.getId(), card, maxX);
                maxX = Math.min(maxX, getMaxXForGraveyardRequirements(gameData, card));
                maxX = Math.min(maxX, getMaxXForDiscardCost(gameData, card));
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
                xValue = hasPermanentManaValueEqualsXTarget(card)
                        ? chosen.getCard().getManaValue()
                        : Math.max(1, chosen.getCard().getManaValue());
            } else {
                int smartX = manaManager.calculateSmartX(gameData, aiPlayer.getId(), card, targetId, virtualPool, costModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                smartX = Math.min(smartX, getMaxXForDiscardCost(gameData, card));
                if (smartX <= 0) {
                    return false;
                }
                xValue = smartX;
            }
        }

        if (exileXCost != null && castCost.hasX() && modalPlan == null) {
            exileGraveyardCardIndices = selectExileXGraveyardIndices(gameData, exileXCost, xValue);
            if (exileGraveyardCardIndices == null) {
                return false;
            }
        }

        if (hasDelveCost(card)) {
            exileGraveyardCardIndices = selectDelveGraveyardIndices(gameData, card, xValue, targetingTax);
            if (exileGraveyardCardIndices == null) {
                return false;
            }
        }
        int delveReduction = hasDelveCost(card) ? exileGraveyardCardIndices.size() : 0;

        if (!canAffordSelectedSpellTarget(
                gameData, card, virtualPool, targetId, multiTargetIds, targetingTax, xValue)) {
            return false;
        }
        BeholdSelection beholdSelection = selectBeholdCost(gameData, card);
        if (beholdSelection == null) {
            return false;
        }
        CostReductionPlan costReductionPlan = selectCostReductionPlan(
                gameData, card, xValue, targetingTax, delveReduction,
                manaManager.buildVirtualManaPool(gameData, aiPlayer.getId()));
        if (costReductionPlan == null) {
            return false;
        }

        log.info("AI (Medium): Casting {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", best.value), gameId);
        Set<UUID> reservedCostPermanentIds = reservedSpellCostPermanentIds(
                sacrificePermanentId, beholdSelection, costReductionPlan);
        if (!canPayManaForSpell(gameData, card, xValue, targetingTax, delveReduction,
                costReductionPlan.reduction(), reservedCostPermanentIds)) {
            return false;
        }
        if (tapManaForSpell(gameData, card, xValue, targetingTax, delveReduction,
                costReductionPlan.reduction(), reservedCostPermanentIds)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
        List<UUID> convokeCreatureIds = selectConvokeCreatureIds(
                gameData, card, xValue, targetingTax, delveReduction);
        if (convokeCreatureIds == null) {
            return false;
        }
        final UUID finalTargetId = targetId;
        final int cardIndex = best.index;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        final Integer finalDiscardHandCardIndex = chooseDiscardCostIndex(
                gameData, card, cardIndex, xValue, targetingTax);
        final List<Integer> finalDiscardHandCardIndices =
                chooseDiscardCostIndices(gameData, card, cardIndex, xValue != null ? xValue : 0);
        final List<UUID> finalMultiSacrificeIds = selectMultiPermanentCostIds(gameData, card);
        final List<UUID> finalImposedSacrificeIds = selectImposedSacrificePermanentIds(
                gameData, card, finalSacrificePermanentId, finalMultiSacrificeIds);
        if (finalImposedSacrificeIds == null) {
            return false;
        }
        final BeholdSelection finalBeholdSelection = beholdSelection;
        send(() -> gameActions.handlePlayCard(
                buildSpellPlayCardRequest(gameData, card, cardIndex, finalXValue, finalTargetId, finalDamageAssignments,
                        finalMultiTargetIds, convokeCreatureIds, costReductionPlan.permanentIds(),
                        finalSacrificePermanentId, null,
                        finalExileGraveyardCardIndices, finalDiscardHandCardIndex,
                        finalDiscardHandCardIndices, finalImposedSacrificeIds, finalMultiSacrificeIds,
                        finalBeholdSelection)));
        // Verify the spell was actually cast — handlePlayCard silently
        // swallows errors, so we must confirm the state actually changed.
        // Identity check: hand size alone is unreliable because ETB/cast triggers
        // can add cards back to hand (e.g. Explore), masking a successful cast.
        if (hand.contains(card)) {
            ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
            log.warn("AI (Medium): PlayCard failed silently in game {}. Card='{}' index={} step={} isActive={} stackEmpty={} pool={} priorityPassed={}",
                    gameId, card.getName(), cardIndex,
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
            case FLASH_CREATURE -> isOpponentsTurn && step == TurnStep.END_STEP;
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
        List<UUID> multiTargetIds = modalPlan != null ? modalPlan.targetIds() : null;
        boolean isMultiTarget = targetSelector.needsMultiTargetSelection(card);
        if (modalPlan == null && !EffectResolution.needsDamageDistribution(card)
                && targetSelector.hasSeparateGraveyardTarget(card)) {
            AiTargetSelector.SpellTargetSelection selection = targetSelector.chooseSeparateGraveyardTargets(
                    gameData, card, aiPlayer.getId());
            if (selection == null) return false;
            targetId = selection.targetId();
            multiTargetIds = selection.targetIds();
        } else if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) return false;
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card)
                && (EffectResolution.needsTarget(card) || card.isAura())
                && !hasPermanentManaValueEqualsXTarget(card)
                && !hasPermanentManaValueAtMostXTarget(card)) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) return false;
        }

        // Check targeting tax (e.g. Kopala, Warden of Waves)
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
        }

        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        List<Integer> exileGraveyardCardIndices = null;
        ExileXCardsFromGraveyardCost exileXCost = findExileXGraveyardCost(card);
        if (exileXCost != null) {
            exileGraveyardCardIndices = selectExileXGraveyardIndices(gameData, exileXCost);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int instantCostModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card) || hasPermanentManaValueAtMostXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, instantCostModifier);
                maxX = manaManager.clampByXValueCap(gameData, aiPlayer.getId(), card, maxX);
                maxX = Math.min(maxX, getMaxXForGraveyardRequirements(gameData, card));
                maxX = Math.min(maxX, getMaxXForDiscardCost(gameData, card));
                if (maxX <= 0) return false;
                List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                        gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) return false;
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = hasPermanentManaValueEqualsXTarget(card)
                        ? chosen.getCard().getManaValue()
                        : Math.max(1, chosen.getCard().getManaValue());
            } else {
                int smartX = manaManager.calculateSmartX(gameData, aiPlayer.getId(), card, targetId, virtualPool, instantCostModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                smartX = Math.min(smartX, getMaxXForDiscardCost(gameData, card));
                if (smartX <= 0) return false;
                xValue = smartX;
            }
        }

        if (exileXCost != null && castCost.hasX() && modalPlan == null) {
            exileGraveyardCardIndices = selectExileXGraveyardIndices(gameData, exileXCost, xValue);
            if (exileGraveyardCardIndices == null) {
                return false;
            }
        }

        if (hasDelveCost(card)) {
            exileGraveyardCardIndices = selectDelveGraveyardIndices(gameData, card, xValue, targetingTax);
            if (exileGraveyardCardIndices == null) {
                return false;
            }
        }
        int delveReduction = hasDelveCost(card) ? exileGraveyardCardIndices.size() : 0;

        if (!canAffordSelectedSpellTarget(
                gameData, card, virtualPool, targetId, multiTargetIds, targetingTax, xValue)) {
            return false;
        }
        BeholdSelection beholdSelection = selectBeholdCost(gameData, card);
        if (beholdSelection == null) {
            return false;
        }
        CostReductionPlan costReductionPlan = selectCostReductionPlan(
                gameData, card, xValue, targetingTax, delveReduction,
                manaManager.buildVirtualManaPool(gameData, aiPlayer.getId()));
        if (costReductionPlan == null) {
            return false;
        }

        log.info("AI (Medium): Casting instant {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", value), gameId);
        Set<UUID> reservedCostPermanentIds = reservedSpellCostPermanentIds(
                sacrificePermanentId, beholdSelection, costReductionPlan);
        if (!canPayManaForSpell(gameData, card, xValue, targetingTax, delveReduction,
                costReductionPlan.reduction(), reservedCostPermanentIds)) {
            return false;
        }
        if (tapManaForSpell(gameData, card, xValue, targetingTax, delveReduction,
                costReductionPlan.reduction(), reservedCostPermanentIds)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
        List<UUID> convokeCreatureIds = selectConvokeCreatureIds(
                gameData, card, xValue, targetingTax, delveReduction);
        if (convokeCreatureIds == null) {
            return false;
        }
        final UUID finalTargetId = targetId;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        final Integer finalDiscardHandCardIndex = chooseDiscardCostIndex(
                gameData, card, cardIndex, xValue, targetingTax);
        final List<Integer> finalDiscardHandCardIndices =
                chooseDiscardCostIndices(gameData, card, cardIndex, xValue != null ? xValue : 0);
        final List<UUID> finalMultiSacrificeIds = selectMultiPermanentCostIds(gameData, card);
        final List<UUID> finalImposedSacrificeIds = selectImposedSacrificePermanentIds(
                gameData, card, finalSacrificePermanentId, finalMultiSacrificeIds);
        if (finalImposedSacrificeIds == null) {
            return false;
        }
        final BeholdSelection finalBeholdSelection = beholdSelection;
        send(() -> gameActions.handlePlayCard(
                buildSpellPlayCardRequest(gameData, card, cardIndex, finalXValue, finalTargetId, finalDamageAssignments,
                        finalMultiTargetIds, convokeCreatureIds, costReductionPlan.permanentIds(),
                        finalSacrificePermanentId, null,
                        finalExileGraveyardCardIndices, finalDiscardHandCardIndex,
                        finalDiscardHandCardIndices, finalImposedSacrificeIds, finalMultiSacrificeIds,
                        finalBeholdSelection)));
        // Identity check: hand size alone is unreliable because ETB/cast triggers
        // can add cards back to hand (e.g. Explore), masking a successful cast.
        if (hand.contains(card)) {
            log.warn("AI (Medium): Instant cast failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    @Override
    protected void handleAttackers(GameData gameData) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, actingPlayerId);
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, actingPlayerId, availableIndices);

        List<Integer> attackerIndices = combatSimulator.findBestAttackers(
                gameData, actingPlayerId, availableIndices, mustAttackIndices);

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

        log.info("AI (Medium): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
        sendAttackerDeclaration(new DeclareAttackersRequest(attackerIndices, null));
    }

    @Override
    protected void handleBlockers(GameData gameData) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        UUID opponentId = AiUtils.getOpponentId(gameData, actingPlayerId);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null) {
            send(() -> gameActions.handleDeclareBlockers(
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
            if (blockLegalityService.canBlock(gameData, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }

        List<int[]> assignments = combatSimulator.findBestBlockers(
                gameData, actingPlayerId, attackerIndices, blockerIndices);

        List<BlockerAssignment> blockerAssignments = assignments.stream()
                .map(a -> new BlockerAssignment(a[0], a[1]))
                .toList();

        log.info("AI (Medium): Declaring {} blockers in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(new DeclareBlockersRequest(blockerAssignments));
    }

    @Override
    protected void handleCardChoice(GameData gameData) {
        if (!(gameData.interaction.activeInteraction() instanceof PendingInteraction.HandChoice cardChoice)) return;

        UUID choicePlayerId = cardChoice.playerId();
        List<Integer> validIndices = cardChoice.validIndices();

        if (!AiUtils.isRespondingFor(gameData, aiPlayer.getId(), choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(choicePlayerId);
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        // Discard the card with the lowest spell value instead of highest mana cost
        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.estimateSpellValue(gameData, hand.get(i), choicePlayerId)))
                .orElse(validIndices.iterator().next());

        log.info("AI (Medium): Discarding card at index {} in game {}", bestIndex, gameId);
        send(() -> gameActions.answerInteraction(new InteractionAnswer.CardIndexChosen(bestIndex)));
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
