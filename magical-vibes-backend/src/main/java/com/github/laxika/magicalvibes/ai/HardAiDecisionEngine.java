package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
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
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hard difficulty AI that uses Information Set Monte Carlo Tree Search (IS-MCTS)
 * to make decisions. Falls back to SpellEvaluator/CombatSimulator-based logic
 * (same as Medium) when MCTS is not applicable (0-1 options) or fails.
 */
@Slf4j
public class HardAiDecisionEngine extends AiDecisionEngine {

    private static final int MCTS_BUDGET = 50000;

    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;
    private final MCTSEngine mctsEngine;

    public HardAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService,
                                CombatAttackService combatAttackService,
                                GameBroadcastService gameBroadcastService,
                                TargetValidationService targetValidationService,
                                TargetLegalityService targetLegalityService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
        BoardEvaluator boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
        this.mctsEngine = new MCTSEngine(new GameSimulator(gameQueryService));
    }

    // ===== Smart Land Selection =====

    /**
     * When multiple lands are in hand, picks the land that maximizes the total
     * value of castable spells this turn. Ties are broken by color coverage —
     * how many colored mana requirements in hand the land helps satisfy.
     */
    @Override
    protected boolean tryPlayLand(GameData gameData) {
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(aiPlayer.getId(), 0);
        if (landsPlayed > 0) {
            return false;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        // Collect all land indices in hand
        List<Integer> landIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).hasType(CardType.LAND)) {
                landIndices.add(i);
            }
        }

        if (landIndices.isEmpty()) {
            return false;
        }

        // If only one land, play it directly
        if (landIndices.size() == 1) {
            return super.tryPlayLand(gameData);
        }

        // Multiple lands: evaluate which one enables the most valuable spells
        VirtualManaPool basePool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Collect non-land spells for evaluation
        List<Card> spells = new ArrayList<>();
        for (Card card : hand) {
            if (!card.hasType(CardType.LAND) && card.getManaCost() != null) {
                spells.add(card);
            }
        }

        int bestLandIndex = landIndices.getFirst();
        double bestSpellValue = -1;
        int bestColorCoverage = -1;

        for (int landIdx : landIndices) {
            Card landCard = hand.get(landIdx);

            // Build hypothetical pool with this land's mana added
            VirtualManaPool hypotheticalPool = new VirtualManaPool(basePool);
            manaManager.addCardManaToPool(landCard, hypotheticalPool);

            // Primary score: total value of castable spells
            double spellValue = 0;
            for (Card spell : spells) {
                if (isSpellCastable(gameData, spell, hypotheticalPool)) {
                    spellValue += spellEvaluator.estimateSpellValue(gameData, spell, aiPlayer.getId());
                }
            }

            // Tiebreaker: how many colored requirements in hand this land helps satisfy
            int colorCoverage = computeColorCoverage(landCard, basePool, spells);

            if (spellValue > bestSpellValue
                    || (spellValue == bestSpellValue && colorCoverage > bestColorCoverage)) {
                bestSpellValue = spellValue;
                bestColorCoverage = colorCoverage;
                bestLandIndex = landIdx;
            }
        }

        log.info("AI (Hard): Playing land {} (best of {} options, spell value={}, coverage={}) in game {}",
                hand.get(bestLandIndex).getName(), landIndices.size(),
                String.format("%.1f", bestSpellValue), bestColorCoverage, gameId);
        int handSizeBefore = hand.size();
        final int idx = bestLandIndex;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(idx, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
        if (hand.size() >= handSizeBefore) {
            log.warn("AI: Land play failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    /**
     * Counts how many colored mana requirements across all spells in hand
     * this land card helps satisfy beyond what the current pool provides.
     */
    private int computeColorCoverage(Card landCard, ManaPool currentPool, List<Card> spells) {
        Set<ManaColor> producedColors = manaManager.getProducedColors(landCard);
        int coverage = 0;
        for (Card spell : spells) {
            ManaCost cost = new ManaCost(spell.getManaCost());
            for (Map.Entry<ManaColor, Integer> entry : cost.getColoredCosts().entrySet()) {
                if (producedColors.contains(entry.getKey())) {
                    int needed = entry.getValue();
                    int have = currentPool.get(entry.getKey());
                    if (have < needed) {
                        coverage += (needed - have);
                    }
                }
            }
        }
        return coverage;
    }

    // ===== Priority / Main Phase =====

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

            if (tryCastSpellWithInstantAwareness(gameData)) {
                return;
            }
        }

        // Try casting instants with timing evaluation
        if (tryCastInstantWithTimingEvaluation(gameData)) {
            return;
        }

        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    /**
     * Wraps sorcery-speed casting with "hold up mana" reasoning. Before committing
     * to a sorcery, checks whether keeping mana open for instants would be higher
     * expected value. If so, skips sorcery casting this priority pass.
     */
    private boolean tryCastSpellWithInstantAwareness(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Find best sorcery-speed spell value
        double bestSorceryValue = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND) || card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            bestSorceryValue = Math.max(bestSorceryValue, value);
        }

        // Find best instant's held value (with timing multiplier)
        double bestInstantHeldValue = evaluateBestHeldInstant(gameData, hand, virtualPool);

        // If holding instant mana is significantly better, skip sorcery casting.
        // The 0.8 factor provides a slight bias toward casting now (bird in hand).
        if (bestSorceryValue > 0 && bestInstantHeldValue > bestSorceryValue * 0.8) {
            log.info("AI (Hard): Holding mana for instant (held={}, sorcery={}) in game {}",
                    String.format("%.1f", bestInstantHeldValue),
                    String.format("%.1f", bestSorceryValue), gameId);
            return false;
        }

        return tryCastSpellMCTS(gameData);
    }

    /**
     * Evaluates the expected value of holding mana for the best instant in hand.
     * Applies timing multipliers based on the instant's category to reflect the
     * value of casting it at the ideal moment later.
     */
    private double evaluateBestHeldInstant(GameData gameData, List<Card> hand, ManaPool virtualPool) {
        double bestValue = 0;
        for (Card card : hand) {
            if (!card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            InstantCategory category = InstantCategoryClassifier.classify(card);

            double baseValue;
            if (category == InstantCategory.COUNTERSPELL) {
                // Counterspells have no board value when nothing is on the stack,
                // but holding mana for them is valuable — estimate based on the
                // counterspell's own mana cost as a proxy for expected threat neutralization.
                baseValue = card.getManaValue() * 4.0;
            } else {
                baseValue = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            }
            if (baseValue <= 0) continue;

            double multiplier = switch (category) {
                case REMOVAL -> 1.3;       // Removal in combat is very strong
                case COMBAT_TRICK -> 1.5;  // Combat tricks create blowouts
                case CARD_ADVANTAGE -> 1.1; // Slightly better to hold for end step
                case BURN_TO_FACE -> 1.0;   // Same value whenever cast
                case COUNTERSPELL -> 1.2;  // Holding counterspell mana is strong
                case OTHER -> 0.8;          // Slight discount for unknown timing
            };

            bestValue = Math.max(bestValue, baseValue * multiplier);
        }
        return bestValue;
    }

    /**
     * Uses MCTS to decide which spell to cast (or whether to pass).
     * Falls back to evaluator-based logic for 0-1 castable options.
     */
    private boolean tryCastSpellMCTS(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Count castable spells
        int castableCount = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND) || card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            castableCount++;
        }

        if (castableCount == 0) {
            return false;
        }

        // In postcombat main with only 1 option, use evaluator (no sequencing benefit).
        // In precombat main, always use MCTS even with 1 option: the rollout spans through
        // combat, so it can evaluate "cast removal now to clear a blocker before attacking"
        // vs "pass to combat and cast postcombat".
        boolean isPrecombat = gameData.currentStep == TurnStep.PRECOMBAT_MAIN;
        if (castableCount == 1 && !isPrecombat) {
            return tryCastSpell(gameData);
        }

        // Use MCTS to decide
        try {
            SimulationAction bestAction = mctsEngine.search(gameData, aiPlayer.getId(), MCTS_BUDGET);

            if (bestAction instanceof SimulationAction.PlayCard pc) {
                Card card = hand.get(pc.handIndex());

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

                // Select sacrifice target if the spell has a sacrifice cost
                UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

                // Select graveyard cards to exile if the spell has a graveyard exile cost
                List<Integer> exileGraveyardCardIndices = null;
                if (findExileXGraveyardCost(card) != null) {
                    exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
                } else if (findExileNGraveyardCost(card) != null) {
                    exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
                }

                ManaCost castCost = new ManaCost(card.getManaCost());
                Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
                UUID mctsTargetId = pc.targetId();
                // Check targeting tax (e.g. Kopala, Warden of Waves)
                int targetingTax = computeTargetingTax(gameData, mctsTargetId, null);
                if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
                    return false;
                }
                int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
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
                        mctsTargetId = chosen.getId();
                        xValue = chosen.getCard().getManaValue();
                    } else {
                        int smartX = manaManager.calculateSmartX(gameData, card, mctsTargetId, virtualPool, costModifier);
                        smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                        if (smartX <= 0) {
                            return false;
                        }
                        xValue = smartX;
                    }
                }
                // Multi-target spells: select per-group targets
                List<UUID> mctsMultiTargetIds = null;
                boolean mctsIsMultiTarget = card.getSpellTargets().size() > 1;
                if (mctsIsMultiTarget && modalPlan == null) {
                    mctsMultiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
                    if (mctsMultiTargetIds == null) {
                        return false;
                    }
                    // Recompute targeting tax with multi-targets
                    int multiTargetTax = computeTargetingTax(gameData, null, mctsMultiTargetIds);
                    if (multiTargetTax > targetingTax) {
                        targetingTax = multiTargetTax;
                        if (!canAffordSpell(gameData, card, virtualPool, targetingTax)) {
                            return false;
                        }
                    }
                    mctsTargetId = null; // Use targetIds, not targetId
                }
                log.info("AI (Hard/MCTS): Casting {}{} in game {}", card.getName(),
                        xValue != null ? " (X=" + xValue + ")" : "", gameId);
                if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
                    return true; // Mana ability triggered a pending choice; will resume after it resolves
                }
                int handSizeBefore = hand.size();
                final int cardIndex = pc.handIndex();
                final UUID targetId = modalPlan != null ? modalPlan.targetId() : (EffectResolution.needsDamageDistribution(card) ? null : mctsTargetId);
                final Integer finalXValue = xValue;
                final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
                final UUID finalSacrificePermanentId = sacrificePermanentId;
                final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
                final List<UUID> finalMctsMultiTargetIds = mctsMultiTargetIds;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(cardIndex, finalXValue, targetId, finalDamageAssignments, finalMctsMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, null, finalExileGraveyardCardIndices, null, null, null)));
                // Verify the spell was actually cast — handlePlayCard silently
                // swallows errors, so we must confirm the state actually changed.
                if (hand.size() >= handSizeBefore) {
                    Card failedCard = hand.size() > cardIndex ? hand.get(cardIndex) : null;
                    ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
                    log.warn("AI (Hard/MCTS): PlayCard failed silently in game {}. Card='{}' index={} step={} isActive={} stackEmpty={} pool={} priorityPassed={}",
                            gameId, failedCard != null ? failedCard.getName() : "?", cardIndex,
                            gameData.currentStep, aiPlayer.getId().equals(gameData.activePlayerId),
                            gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                            gameData.priorityPassedBy);
                    return false;
                }
                return true;
            }

            if (bestAction instanceof SimulationAction.PassPriority) {
                log.info("AI (Hard/MCTS): MCTS recommends passing in game {}", gameId);
                return false; // Let handleGameState send the pass
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS failed, falling back to evaluator logic in game {}", gameId, e);
            return tryCastSpell(gameData);
        }

        return false;
    }

    // ===== Spell Casting (evaluator-based fallback) =====

    private boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        record CastCandidate(int index, double value) {}
        List<CastCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) continue;
            if (card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                candidates.add(new CastCandidate(i, value));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

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

        // Check targeting tax (e.g. Kopala, Warden of Waves)
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
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

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
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

        log.info("AI (Hard): Casting {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", best.value), gameId);
        if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
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
            log.warn("AI (Hard): PlayCard failed silently in game {}. Card='{}' index={} step={} isActive={} stackEmpty={} pool={} priorityPassed={}",
                    gameId, failedCard != null ? failedCard.getName() : "?", cardIndex,
                    gameData.currentStep, aiPlayer.getId().equals(gameData.activePlayerId),
                    gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                    gameData.priorityPassedBy);
            return false;
        }
        return true;
    }

    // ===== Instant Casting with Timing Evaluation =====

    /**
     * Tries to cast the best instant using category-based timing with value multipliers.
     * Only casts if the timing-adjusted value exceeds a minimum threshold.
     */
    private boolean tryCastInstantWithTimingEvaluation(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        boolean isOpponentsTurn = !aiPlayer.getId().equals(gameData.activePlayerId);
        TurnStep step = gameData.currentStep;

        record TimedCandidate(int index, double adjustedValue) {}
        List<TimedCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            InstantCategory category = InstantCategoryClassifier.classify(card);
            if (!isGoodTimingForHard(category, step, isOpponentsTurn)) continue;

            // Counterspells need a valid target on the stack
            if (category == InstantCategory.COUNTERSPELL) {
                UUID spellTargetId = targetSelector.chooseSpellTarget(gameData, card, aiPlayer.getId());
                if (spellTargetId == null) continue;
                double baseValue = evaluateCounterspellValue(gameData, card, spellTargetId);
                if (baseValue <= 0) continue;
                double timingMultiplier = getTimingMultiplier(category, step, isOpponentsTurn);
                double adjustedValue = baseValue * timingMultiplier;
                if (adjustedValue >= 5.0) {
                    candidates.add(new TimedCandidate(i, adjustedValue));
                }
                continue;
            }

            double baseValue = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (baseValue <= 0) continue;

            double timingMultiplier = getTimingMultiplier(category, step, isOpponentsTurn);
            double adjustedValue = baseValue * timingMultiplier;

            // Only cast if the timing-adjusted value meets a minimum threshold
            if (adjustedValue >= 5.0) {
                candidates.add(new TimedCandidate(i, adjustedValue));
            }
        }

        if (candidates.isEmpty()) return false;

        candidates.sort(Comparator.comparingDouble(TimedCandidate::adjustedValue).reversed());
        TimedCandidate best = candidates.getFirst();
        return castInstantAtIndex(gameData, hand, best.index, best.adjustedValue);
    }

    /**
     * Determines whether the current game state is a good time to cast an instant
     * of the given category. Same timing windows as Medium AI but with additional
     * flexibility (e.g. removal also good at end step as fallback).
     */
    private boolean isGoodTimingForHard(InstantCategory category, TurnStep step, boolean isOpponentsTurn) {
        return switch (category) {
            case REMOVAL -> isOpponentsTurn
                    && (step == TurnStep.BEGINNING_OF_COMBAT
                    || step == TurnStep.DECLARE_ATTACKERS
                    || step == TurnStep.DECLARE_BLOCKERS
                    || step == TurnStep.END_STEP);
            case BURN_TO_FACE -> isOpponentsTurn && step == TurnStep.END_STEP;
            case CARD_ADVANTAGE -> isOpponentsTurn && step == TurnStep.END_STEP;
            case COMBAT_TRICK -> !isOpponentsTurn
                    && (step == TurnStep.DECLARE_BLOCKERS || step == TurnStep.COMBAT_DAMAGE);
            case COUNTERSPELL -> true; // Always ready — actual filtering is done by target selection
            case OTHER -> step == TurnStep.PRECOMBAT_MAIN || step == TurnStep.POSTCOMBAT_MAIN
                    || (isOpponentsTurn && step == TurnStep.END_STEP);
        };
    }

    /**
     * Returns a timing multiplier that reflects how valuable it is to cast
     * an instant of the given category at the current step.
     */
    private double getTimingMultiplier(InstantCategory category, TurnStep step, boolean isOpponentsTurn) {
        return switch (category) {
            case REMOVAL -> {
                if (step == TurnStep.DECLARE_BLOCKERS) yield 1.4; // Best: after blocks
                if (step == TurnStep.DECLARE_ATTACKERS) yield 1.3; // Good: kill attacker
                if (step == TurnStep.BEGINNING_OF_COMBAT) yield 1.2;
                yield 1.0; // End step fallback
            }
            case COMBAT_TRICK -> {
                if (step == TurnStep.DECLARE_BLOCKERS) yield 1.5; // Blowout potential
                yield 1.2;
            }
            case CARD_ADVANTAGE -> 1.1;
            case BURN_TO_FACE -> 1.0;
            case COUNTERSPELL -> 1.3; // Countering a spell is very high value
            case OTHER -> 0.9;
        };
    }

    /**
     * Evaluates the value of countering a specific spell on the stack.
     * High-value spells (big creatures, board wipes, removal) are worth more to counter.
     * Applies a minimum threshold: don't waste a counterspell on a low-impact spell
     * unless the AI is at low life (then even cheap spells become worth countering).
     */
    private double evaluateCounterspellValue(GameData gameData, Card counterSpell, UUID spellTargetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard().getId().equals(spellTargetId)) {
                Card targetCard = entry.getCard();

                // Don't waste a counterspell on a spell that costs less than the counterspell itself,
                // unless the AI is at critically low life. We want mana-efficient exchanges.
                int aiLife = gameData.playerLifeTotals.getOrDefault(aiPlayer.getId(), 20);
                int counterManaValue = counterSpell.getManaValue();
                if (aiLife > 5 && targetCard.getManaValue() < counterManaValue) {
                    return 0;
                }

                double value = targetCard.getManaValue() * 5.0;

                // Creatures are valued by stats + mana value
                if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
                    int power = targetCard.getPower() != null ? targetCard.getPower() : 0;
                    int toughness = targetCard.getToughness() != null ? targetCard.getToughness() : 0;
                    value += power * 3.0 + toughness * 1.5;
                }

                return value;
            }
        }
        return 0;
    }

    /**
     * Casts the instant at the given hand index. Handles targeting, mana tapping,
     * sacrifice costs, graveyard exile, and X-value calculation.
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
        } else if (modalPlan == null && EffectResolution.needsSpellTarget(card)) {
            // Counterspells target a spell on the stack
            targetId = targetSelector.chooseSpellTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) return false;
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
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
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int instantCostModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
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

        log.info("AI (Hard): Casting instant {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", value), gameId);
        if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
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
            log.warn("AI (Hard): Instant cast failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    // ===== Combat =====

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        if (availableIndices.isEmpty()) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                    new DeclareAttackersRequest(List.of(), null)));
            return;
        }

        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);

        // If 0-1 attackers, use CombatSimulator (no need for MCTS)
        if (availableIndices.size() <= 1) {
            handleAttackersWithSimulator(gameData, availableIndices, mustAttackIndices);
            return;
        }

        // Use MCTS for attacker selection
        try {
            SimulationAction bestAction = mctsEngine.search(gameData, aiPlayer.getId(), MCTS_BUDGET);

            if (bestAction instanceof SimulationAction.DeclareAttackers da) {
                // Ensure must-attack creatures are included in the MCTS result
                List<Integer> attackerIndices = enforceMustAttack(da.attackerIndices(), mustAttackIndices);
                // Ensure at least one attacker when forced (e.g. Trove of Temptation)
                attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);
                // Cap attackers to what we can afford given attack tax, and tap mana to pay
                attackerIndices = prepareAttackersForTax(gameData, attackerIndices);
                log.info("AI (Hard/MCTS): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
                final List<Integer> finalAttackerIndices = attackerIndices;
                send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                        new DeclareAttackersRequest(finalAttackerIndices, null)));
                return;
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS attacker search failed, falling back to evaluator logic", e);
        }

        // Fall back to CombatSimulator
        handleAttackersWithSimulator(gameData, availableIndices, mustAttackIndices);
    }

    private void handleAttackersWithSimulator(GameData gameData, List<Integer> availableIndices,
                                              List<Integer> mustAttackIndices) {
        List<Integer> attackerIndices = combatSimulator.findBestAttackers(
                gameData, aiPlayer.getId(), availableIndices, mustAttackIndices);

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

        log.info("AI (Hard): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
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

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                attackerIndices.add(i);
            }
        }

        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gameData, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }

        List<int[]> assignments = combatSimulator.findBestBlockersExhaustive(
                gameData, aiPlayer.getId(), attackerIndices, blockerIndices);

        List<BlockerAssignment> blockerAssignments = assignments.stream()
                .map(a -> new BlockerAssignment(a[0], a[1]))
                .toList();

        log.info("AI (Hard): Declaring {} blockers (exhaustive search) in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
    }

    // ===== Card Choice (discard lowest spell value) =====

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) return;

        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.estimateSpellValue(gameData, hand.get(i), aiPlayer.getId())))
                .orElse(validIndices.iterator().next());

        log.info("AI (Hard): Discarding card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    // ===== Mulligan (scoring-based) =====

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return true;

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) return true;

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        if (landCount == 0 && mulliganCount < 2) return false;
        if (landCount > 5) return false;

        // Collect all mana colors producible by lands in hand
        Set<ManaColor> availableColors = EnumSet.noneOf(ManaColor.class);
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                availableColors.addAll(manaManager.getProducedColors(card));
            }
        }

        double handScore = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                handScore += 1.5;
                continue;
            }

            // Check if the spell's colored requirements can be met by available lands
            boolean colorCastable = isColorCastable(card, availableColors);

            int mv = card.getManaValue();
            if (!colorCastable) {
                // Spell requires colors our lands can't produce — nearly dead card
                handScore += 0.25;
            } else if (mv <= landCount + 1) {
                handScore += 3.0;
            } else if (mv <= landCount + 3) {
                handScore += 1.5;
            } else {
                handScore += 0.5;
            }
        }

        double threshold = 12.0 - mulliganCount * 3.0;
        return handScore >= threshold;
    }

    /**
     * Returns true if all colored mana requirements of the spell can be produced
     * by the given set of available colors. Colorless and generic costs are always
     * satisfiable. Phyrexian mana is ignored since it can be paid with life.
     */
    private boolean isColorCastable(Card spell, Set<ManaColor> availableColors) {
        if (spell.getManaCost() == null || spell.getManaCost().isEmpty()) return true;
        ManaCost cost = new ManaCost(spell.getManaCost());
        for (ManaColor required : cost.getColoredCosts().keySet()) {
            if (!availableColors.contains(required)) {
                return false;
            }
        }
        return true;
    }
}
