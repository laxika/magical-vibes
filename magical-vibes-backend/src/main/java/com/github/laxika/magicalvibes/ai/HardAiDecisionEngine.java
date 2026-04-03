package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
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
    private final BoardEvaluator boardEvaluator;
    private final CombatSimulator combatSimulator;
    private final MCTSEngine mctsEngine;
    private final RaceEvaluator raceEvaluator;

    public HardAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService,
                                CombatAttackService combatAttackService,
                                GameBroadcastService gameBroadcastService,
                                TargetValidationService targetValidationService,
                                TargetLegalityService targetLegalityService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
        this.mctsEngine = new MCTSEngine(new GameSimulator(gameQueryService));
        this.raceEvaluator = new RaceEvaluator(gameQueryService);
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

            if (gameData.currentStep == TurnStep.PRECOMBAT_MAIN) {
                // Before normal spell casting, check if burn spells in hand can deal lethal
                if (tryBurnToFaceLethal(gameData)) {
                    return;
                }

                // Explicit precombat heuristics: removal to clear blockers for lethal,
                // lords to pump attackers, haste creatures that can join the attack
                if (tryCastPrecombatPrioritySpell(gameData)) {
                    return;
                }

                // Broader combat-relevant spells: removal for damage gain (not just lethal),
                // lords/anthems that meaningfully pump, any haste creature.
                // Non-combat spells (card draw, non-haste creatures, enchantments) are
                // deferred to postcombat main where they don't delay the attack.
                if (tryCastCombatRelevantSpellPrecombat(gameData)) {
                    return;
                }

                // If we have potential attackers, defer non-combat spells to postcombat
                // so we don't waste time before combat. If we have no attackers, combat
                // is irrelevant and we can cast everything now.
                if (hasPotentialAttackers(gameData)) {
                    // Skip general sorcery casting — defer to postcombat
                } else {
                    if (tryCastSpellWithInstantAwareness(gameData)) {
                        return;
                    }
                }
            } else {
                // Postcombat: cast all remaining spells
                if (tryCastSpellWithInstantAwareness(gameData)) {
                    return;
                }
            }
        }

        // Try casting instants with timing evaluation
        if (tryCastInstantWithTimingEvaluation(gameData)) {
            return;
        }

        // Try activated abilities on controlled permanents
        if (tryActivateAbility(gameData)) {
            return;
        }

        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    // ===== Precombat vs Postcombat Timing =====

    /**
     * Combat relevance categories for sorcery-speed spells, used to decide
     * whether to cast a spell precombat or defer to postcombat.
     */
    private enum CombatRelevance {
        /** Removal that clears blockers or steals creatures */
        REMOVAL,
        /** Lord/anthem that pumps existing attackers */
        PUMP,
        /** Creature with haste that can attack immediately */
        HASTE_CREATURE,
        /** Not directly combat-relevant — better cast postcombat */
        NON_COMBAT
    }

    /**
     * Tries to cast a combat-relevant spell before combat using explicit heuristics
     * that complement MCTS. Checks three scenarios in order:
     * <ol>
     *   <li>Removal that clears a blocker for a lethal or near-lethal attack</li>
     *   <li>Lord/anthem creature that pumps existing attackers toward lethal</li>
     *   <li>Haste creature whose power pushes the attack to lethal or near-lethal</li>
     * </ol>
     */
    private boolean tryCastPrecombatPrioritySpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        int opponentLife = gameData.getLife(opponentId);

        // Gather attackers: untapped, non-defender, combat-ready creatures
        List<Permanent> attackers = new ArrayList<>();
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            attackers.add(perm);
        }

        // Gather opponent's potential blockers: untapped creatures
        List<Permanent> blockers = new ArrayList<>();
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(opponentId, List.of())) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            blockers.add(perm);
        }

        // Tally castable burn damage for near-lethal checks (attack + burn = lethal)
        int burnInHand = 0;
        for (Card card : hand) {
            if (card.getManaCost() == null) continue;
            burnInHand += raceEvaluator.getBurnToFaceDamage(card);
        }

        // 1. Removal that clears a blocker for lethal / near-lethal attack
        if (!attackers.isEmpty() && !blockers.isEmpty()) {
            if (tryRemovalForLethal(gameData, hand, virtualPool, attackers, blockers,
                    opponentLife, burnInHand)) {
                return true;
            }
        }

        // 2. Lord/anthem that pumps existing attackers toward lethal
        if (!attackers.isEmpty()) {
            if (tryLordBeforeCombat(gameData, hand, virtualPool, attackers, blockers,
                    opponentLife, burnInHand)) {
                return true;
            }
        }

        // 3. Haste creature whose power pushes attack to lethal / near-lethal
        if (tryHasteCreatureBeforeCombat(gameData, hand, virtualPool, attackers, blockers,
                opponentLife, burnInHand)) {
            return true;
        }

        return false;
    }

    /**
     * Casts the best combat-relevant sorcery-speed spell in precombat main,
     * even when it does not enable lethal. This covers three categories:
     * <ul>
     *   <li><b>Removal</b> — clears a blocker if the damage gain is significant
     *       (≥ 2 extra damage through)</li>
     *   <li><b>Pump</b> — lord/anthem creature that boosts existing attackers by
     *       ≥ 2 total power</li>
     *   <li><b>Haste creature</b> — any haste creature that can join the attack</li>
     * </ul>
     * Non-combat spells (card draw, non-haste creatures, enchantments) are skipped
     * so they can be cast in postcombat main instead.
     */
    private boolean tryCastCombatRelevantSpellPrecombat(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());

        // Gather combat state
        List<Permanent> attackers = new ArrayList<>();
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            attackers.add(perm);
        }
        List<Permanent> blockers = new ArrayList<>();
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(opponentId, List.of())) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            blockers.add(perm);
        }

        record PrecombatCandidate(int handIndex, double value, CombatRelevance relevance,
                                  UUID targetId) {}
        List<PrecombatCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (isInstantSpeedCard(card) || card.hasType(CardType.LAND)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            CombatRelevance relevance = classifyCombatRelevance(card);
            if (relevance == CombatRelevance.NON_COMBAT) continue;

            switch (relevance) {
                case REMOVAL -> {
                    if (attackers.isEmpty() || blockers.isEmpty()) break;
                    int currentUnblockable = estimateUnblockableDamage(gameData, attackers, blockers);
                    UUID bestBlockerTarget = null;
                    int bestDamageGain = 0;
                    for (Permanent blocker : blockers) {
                        if (!canEffectRemoveCreature(gameData, card, blocker)) continue;
                        List<Permanent> remaining = new ArrayList<>(blockers);
                        remaining.removeIf(b -> b.getId().equals(blocker.getId()));
                        int newUnblockable = estimateUnblockableDamage(gameData, attackers, remaining);
                        int gain = newUnblockable - currentUnblockable;
                        if (gain > bestDamageGain) {
                            bestDamageGain = gain;
                            bestBlockerTarget = blocker.getId();
                        }
                    }
                    if (bestDamageGain >= 2 && bestBlockerTarget != null) {
                        double value = spellEvaluator.estimateSpellValue(
                                gameData, card, aiPlayer.getId());
                        candidates.add(new PrecombatCandidate(i, value + bestDamageGain,
                                relevance, bestBlockerTarget));
                    }
                }
                case PUMP -> {
                    if (attackers.isEmpty()) break;
                    int totalBoost = 0;
                    for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof StaticBoostEffect boost && boost.powerBoost() > 0
                                && (boost.scope() == GrantScope.OWN_CREATURES
                                    || boost.scope() == GrantScope.ALL_OWN_CREATURES)) {
                            for (Permanent attacker : attackers) {
                                if (boost.filter() == null
                                        || gameQueryService.matchesPermanentPredicate(
                                                gameData, attacker, boost.filter())) {
                                    totalBoost += boost.powerBoost();
                                }
                            }
                        }
                    }
                    if (totalBoost >= 2) {
                        double value = spellEvaluator.estimateSpellValue(
                                gameData, card, aiPlayer.getId());
                        candidates.add(new PrecombatCandidate(i, value, relevance, null));
                    }
                }
                case HASTE_CREATURE -> {
                    if (card.getPower() != null && card.getPower() >= 1) {
                        double value = spellEvaluator.estimateSpellValue(
                                gameData, card, aiPlayer.getId());
                        candidates.add(new PrecombatCandidate(i, value, relevance, null));
                    }
                }
                default -> { /* NON_COMBAT already skipped above */ }
            }
        }

        if (candidates.isEmpty()) return false;

        // Respect instant-holding: compare "cast precombat spell + hold remaining instants"
        // vs "skip spell and hold all instants". Considers multiple instants, not just the best.
        List<HeldInstantCandidate> heldInstants = evaluateHeldInstants(gameData, hand, virtualPool);
        candidates.sort(Comparator.comparingDouble(PrecombatCandidate::value).reversed());
        PrecombatCandidate best = candidates.getFirst();

        if (!heldInstants.isEmpty()) {
            Card card = hand.get(best.handIndex);
            int costModifier = gameBroadcastService.getCastCostModifier(
                    gameData, aiPlayer.getId(), card);
            int spellCost = Math.max(0, card.getManaValue() + costModifier);
            int totalMana = virtualPool.getTotal();

            // Value of skipping the spell: greedily fit the most valuable instants
            double holdOnlyValue = 0;
            int holdOnlyCost = 0;
            for (HeldInstantCandidate instant : heldInstants) {
                if (holdOnlyCost + instant.effectiveCost() <= totalMana) {
                    holdOnlyCost += instant.effectiveCost();
                    holdOnlyValue += instant.heldValue();
                }
            }

            // Value of casting the spell: spell value + greedily fit instants in remaining mana
            int manaAfterSpell = totalMana - spellCost;
            double castPlusHoldValue = best.value;
            int castPlusHoldCost = 0;
            for (HeldInstantCandidate instant : heldInstants) {
                if (castPlusHoldCost + instant.effectiveCost() <= manaAfterSpell) {
                    castPlusHoldCost += instant.effectiveCost();
                    castPlusHoldValue += instant.heldValue();
                }
            }

            // Discount held instant value by 0.8 (uncertain whether they'll be needed)
            double adjustedHoldOnly = holdOnlyValue * 0.8;
            double adjustedCastPlusHold = best.value + (castPlusHoldValue - best.value) * 0.8;

            if (adjustedHoldOnly > adjustedCastPlusHold) {
                int instantsHeld = (int) heldInstants.stream()
                        .filter(i -> i.effectiveCost() <= totalMana).count();
                log.info("AI (Hard): Holding mana for {} instant(s) instead of precombat {} in game {}",
                        instantsHeld, card.getName(), gameId);
                return false;
            }
        }

        log.info("AI (Hard): Casting {} precombat (combat-relevant: {}, value={}) in game {}",
                hand.get(best.handIndex).getName(), best.relevance,
                String.format("%.1f", best.value), gameId);
        return tryCastSpecificSpell(gameData, best.handIndex, best.targetId);
    }

    /**
     * Checks if a sorcery-speed removal spell can clear a blocker and enable lethal
     * (or near-lethal within burn range) combat damage. If so, casts the removal
     * targeting that specific blocker.
     */
    private boolean tryRemovalForLethal(GameData gameData, List<Card> hand, ManaPool virtualPool,
                                         List<Permanent> attackers, List<Permanent> blockers,
                                         int opponentLife, int burnInHand) {
        int currentUnblockable = estimateUnblockableDamage(gameData, attackers, blockers);
        // Already lethal without removal — just attack, don't waste removal
        if (currentUnblockable >= opponentLife) return false;

        record RemovalCandidate(int handIndex, Permanent targetBlocker, int damageGain) {}
        List<RemovalCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (isInstantSpeedCard(card) || card.hasType(CardType.LAND)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            if (classifyCombatRelevance(card) != CombatRelevance.REMOVAL) continue;

            for (Permanent blocker : blockers) {
                if (!canEffectRemoveCreature(gameData, card, blocker)) continue;

                List<Permanent> remainingBlockers = new ArrayList<>(blockers);
                remainingBlockers.removeIf(b -> b.getId().equals(blocker.getId()));
                int newUnblockable = estimateUnblockableDamage(gameData, attackers, remainingBlockers);
                int damageGain = newUnblockable - currentUnblockable;

                // Cast if removal enables lethal (board alone or board + burn)
                if (newUnblockable >= opponentLife
                        || newUnblockable + burnInHand >= opponentLife) {
                    candidates.add(new RemovalCandidate(i, blocker, damageGain));
                }
            }
        }

        if (candidates.isEmpty()) return false;

        RemovalCandidate best = candidates.stream()
                .max(Comparator.comparingInt(RemovalCandidate::damageGain))
                .orElse(null);
        if (best == null) return false;

        log.info("AI (Hard): Casting removal precombat to clear {} for lethal attack " +
                        "(damage gain={}) in game {}",
                best.targetBlocker.getCard().getName(), best.damageGain, gameId);

        return tryCastSpecificSpell(gameData, best.handIndex, best.targetBlocker.getId());
    }

    /**
     * Checks if casting a lord/anthem creature precombat would pump existing attackers
     * enough to enable lethal or near-lethal damage. If so, casts the lord.
     */
    private boolean tryLordBeforeCombat(GameData gameData, List<Card> hand, ManaPool virtualPool,
                                         List<Permanent> attackers, List<Permanent> blockers,
                                         int opponentLife, int burnInHand) {
        int currentUnblockable = estimateUnblockableDamage(gameData, attackers, blockers);

        record LordCandidate(int handIndex, int totalPowerBoost) {}
        List<LordCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.CREATURE)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            // Check for static power boost effects (lord/anthem)
            int totalBoost = 0;
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof StaticBoostEffect boost && boost.powerBoost() > 0
                        && (boost.scope() == GrantScope.OWN_CREATURES
                            || boost.scope() == GrantScope.ALL_OWN_CREATURES)) {
                    for (Permanent attacker : attackers) {
                        if (boost.filter() == null
                                || gameQueryService.matchesPermanentPredicate(
                                        gameData, attacker, boost.filter())) {
                            totalBoost += boost.powerBoost();
                        }
                    }
                }
            }

            if (totalBoost <= 0) continue;

            // If the lord has haste, its own power also contributes to the attack
            int lordAttackPower = 0;
            if (card.getKeywords().contains(Keyword.HASTE)
                    && card.getPower() != null && card.getPower() > 0) {
                lordAttackPower = card.getPower();
            }

            int projectedDamage = currentUnblockable + totalBoost;
            if (projectedDamage >= opponentLife
                    || projectedDamage + burnInHand >= opponentLife) {
                candidates.add(new LordCandidate(i, totalBoost + lordAttackPower));
            } else if (totalBoost >= 3) {
                // Significant pump even if not lethal (e.g. +1/+1 to 3+ creatures)
                candidates.add(new LordCandidate(i, totalBoost + lordAttackPower));
            }
        }

        if (candidates.isEmpty()) return false;

        LordCandidate best = candidates.stream()
                .max(Comparator.comparingInt(LordCandidate::totalPowerBoost))
                .orElse(null);
        if (best == null) return false;

        log.info("AI (Hard): Casting lord precombat to pump attackers " +
                "(total boost={}) in game {}", best.totalPowerBoost, gameId);

        return tryCastSpecificSpell(gameData, best.handIndex, null);
    }

    /**
     * Checks if a haste creature in hand would push attack damage to lethal or
     * near-lethal (within burn range). If so, casts it precombat.
     */
    private boolean tryHasteCreatureBeforeCombat(GameData gameData, List<Card> hand,
                                                  ManaPool virtualPool,
                                                  List<Permanent> attackers,
                                                  List<Permanent> blockers,
                                                  int opponentLife, int burnInHand) {
        int currentUnblockable = estimateUnblockableDamage(gameData, attackers, blockers);

        record HasteCandidate(int handIndex, int power) {}
        List<HasteCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.CREATURE)) continue;
            if (!card.getKeywords().contains(Keyword.HASTE)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            if (card.getPower() == null || card.getPower() <= 0) continue;

            int power = card.getPower();
            // Haste creature's power adds to the attack (opponent may block it,
            // but it frees up another attacker — net effect ≈ +power to face)
            int projectedDamage = currentUnblockable + power;
            if (projectedDamage >= opponentLife
                    || projectedDamage + burnInHand >= opponentLife) {
                candidates.add(new HasteCandidate(i, power));
            }
        }

        if (candidates.isEmpty()) return false;

        HasteCandidate best = candidates.stream()
                .max(Comparator.comparingInt(HasteCandidate::power))
                .orElse(null);
        if (best == null) return false;

        log.info("AI (Hard): Casting haste creature precombat to attack " +
                "(power={}) in game {}", best.power, gameId);

        return tryCastSpecificSpell(gameData, best.handIndex, null);
    }

    /**
     * Estimates how much combat damage would get through to the opponent's face.
     * Simple model: opponent blocks the strongest attackers first; remaining
     * attackers deal damage unblocked. Accounts for trample on blocked attackers.
     */
    private int estimateUnblockableDamage(GameData gameData,
                                           List<Permanent> attackers,
                                           List<Permanent> blockers) {
        if (attackers.isEmpty()) return 0;
        if (blockers.isEmpty()) {
            return attackers.stream()
                    .mapToInt(p -> Math.max(0, gameQueryService.getEffectivePower(gameData, p)))
                    .sum();
        }

        // Sort attackers by power descending — opponent blocks strongest first
        List<Permanent> sortedAttackers = new ArrayList<>(attackers);
        sortedAttackers.sort(Comparator.comparingInt(
                (Permanent p) -> gameQueryService.getEffectivePower(gameData, p)).reversed());

        // Sort blockers by toughness descending (assigned to strongest attackers)
        List<Permanent> sortedBlockers = new ArrayList<>(blockers);
        sortedBlockers.sort(Comparator.comparingInt(
                (Permanent p) -> gameQueryService.getEffectiveToughness(gameData, p)).reversed());

        int blockerCount = Math.min(sortedBlockers.size(), sortedAttackers.size());
        int unblockableDamage = 0;

        // Unblocked attackers deal full damage to face
        for (int i = blockerCount; i < sortedAttackers.size(); i++) {
            unblockableDamage += Math.max(0,
                    gameQueryService.getEffectivePower(gameData, sortedAttackers.get(i)));
        }

        // Blocked attackers with trample deal excess damage through
        for (int i = 0; i < blockerCount; i++) {
            Permanent attacker = sortedAttackers.get(i);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.TRAMPLE)) {
                int attackerPower = gameQueryService.getEffectivePower(gameData, attacker);
                int blockerToughness = gameQueryService.getEffectiveToughness(
                        gameData, sortedBlockers.get(i));
                int trampleDamage = attackerPower - blockerToughness;
                if (trampleDamage > 0) {
                    unblockableDamage += trampleDamage;
                }
            }
        }

        return unblockableDamage;
    }

    /**
     * Returns true if the AI controls at least one creature that could attack
     * this turn (untapped, not defender, not summoning sick unless it has haste).
     * Used to decide whether to defer non-combat spells to postcombat.
     */
    private boolean hasPotentialAttackers(GameData gameData) {
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            return true;
        }
        return false;
    }

    /**
     * Classifies a sorcery-speed spell's combat relevance to determine whether it
     * should be cast precombat (affects the attack) or can wait until postcombat.
     */
    private CombatRelevance classifyCombatRelevance(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isSingleEffectRemoval(effect)) return CombatRelevance.REMOVAL;
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (isSingleEffectRemoval(effect)) return CombatRelevance.REMOVAL;
        }

        if (card.hasType(CardType.CREATURE)) {
            if (card.getKeywords().contains(Keyword.HASTE)) return CombatRelevance.HASTE_CREATURE;

            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof StaticBoostEffect boost && boost.powerBoost() > 0
                        && (boost.scope() == GrantScope.OWN_CREATURES
                            || boost.scope() == GrantScope.ALL_OWN_CREATURES)) {
                    return CombatRelevance.PUMP;
                }
            }
        }

        return CombatRelevance.NON_COMBAT;
    }

    private boolean isSingleEffectRemoval(CardEffect effect) {
        if (effect instanceof ChooseOneEffect coe) {
            for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                if (isSingleEffectRemoval(option.effect())) return true;
            }
            return false;
        }
        return effect instanceof DestroyTargetPermanentEffect
                || effect instanceof ExileTargetPermanentEffect
                || effect instanceof ReturnTargetPermanentToHandEffect
                || effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect
                || effect instanceof DealDamageToTargetCreatureEffect
                || effect instanceof DealDamageToTargetCreatureOrPlaneswalkerEffect
                || effect instanceof DealDamageToAnyTargetEffect
                || effect instanceof GainControlOfTargetPermanentEffect
                || effect instanceof GainControlOfTargetPermanentUntilEndOfTurnEffect;
    }

    /**
     * Checks whether a removal spell's effects can remove a specific creature.
     * Accounts for hexproof, shroud, indestructible, and damage vs toughness.
     */
    private boolean canEffectRemoveCreature(GameData gameData, Card spell, Permanent creature) {
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.HEXPROOF)) return false;
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.SHROUD)) return false;

        for (CardEffect effect : spell.getEffects(EffectSlot.SPELL)) {
            if (canSingleEffectRemoveCreature(gameData, effect, creature)) return true;
        }
        for (CardEffect effect : spell.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (canSingleEffectRemoveCreature(gameData, effect, creature)) return true;
        }
        return false;
    }

    private boolean canSingleEffectRemoveCreature(GameData gameData, CardEffect effect,
                                                   Permanent creature) {
        if (effect instanceof ChooseOneEffect coe) {
            for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                if (canSingleEffectRemoveCreature(gameData, option.effect(), creature)) return true;
            }
            return false;
        }
        if (effect instanceof DestroyTargetPermanentEffect) {
            return !gameQueryService.hasKeyword(gameData, creature, Keyword.INDESTRUCTIBLE);
        }
        if (effect instanceof ExileTargetPermanentEffect) return true;
        if (effect instanceof ReturnTargetPermanentToHandEffect) return true;
        if (effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect) return true;
        if (effect instanceof GainControlOfTargetPermanentEffect) return true;
        if (effect instanceof GainControlOfTargetPermanentUntilEndOfTurnEffect) return true;
        if (effect instanceof DealDamageToTargetCreatureEffect dmg) {
            int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
            return dmg.damage() >= toughness - creature.getMarkedDamage();
        }
        if (effect instanceof DealDamageToTargetCreatureOrPlaneswalkerEffect dmg) {
            int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
            return dmg.damage() >= toughness - creature.getMarkedDamage();
        }
        if (effect instanceof DealDamageToAnyTargetEffect dmg) {
            int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
            return dmg.damage() >= toughness - creature.getMarkedDamage();
        }
        return false;
    }

    /**
     * Casts a specific spell from hand. Used by precombat heuristics that have
     * already decided which spell to cast. Handles targeting, modal spells,
     * X costs, sacrifice costs, etc.
     *
     * @param forcedTargetId if non-null, overrides targetSelector choice
     */
    private boolean tryCastSpecificSpell(GameData gameData, int handIndex, UUID forcedTargetId) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        Card card = hand.get(handIndex);
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Handle modal spells
        ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
        if (modalPlan == null && findChooseOneEffect(card) != null) {
            return false;
        }

        // Handle damage distribution
        Map<UUID, Integer> damageAssignments = null;
        if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
            damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
            if (damageAssignments == null) return false;
        }

        // Targeting
        UUID targetId = forcedTargetId;
        List<UUID> multiTargetIds = null;
        boolean isMultiTarget = card.getSpellTargets().size() > 1;
        if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) return false;
        } else if (targetId == null && modalPlan != null) {
            targetId = modalPlan.targetId();
        } else if (targetId == null && !EffectResolution.needsDamageDistribution(card)
                && (EffectResolution.needsTarget(card) || card.isAura())) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) return false;
        }

        // Targeting tax
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
        }

        // Sacrifice cost
        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        // Graveyard exile cost
        List<Integer> exileGraveyardCardIndices = null;
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(
                    gameData, findExileNGraveyardCost(card));
        }

        // X value
        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int costModifier = gameBroadcastService.getCastCostModifier(
                gameData, aiPlayer.getId(), card) + targetingTax;
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                if (maxX <= 0) return false;
                List<Permanent> validTargets =
                        targetSelector.findValidPermanentTargetsForManaValueX(
                                gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) return false;
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = chosen.getCard().getManaValue();
            } else {
                int smartX = manaManager.calculateSmartX(
                        gameData, card, targetId, virtualPool, costModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                if (smartX <= 0) return false;
                xValue = smartX;
            }
        }

        // Cast
        if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
            return true; // Mana ability triggered a pending choice
        }
        int handSizeBefore = hand.size();
        final int idx = handIndex;
        final UUID fTargetId = targetId;
        final Integer fXValue = xValue;
        final Map<UUID, Integer> fDamageAssignments = damageAssignments;
        final UUID fSacrificePermanentId = sacrificePermanentId;
        final List<Integer> fExileIndices = exileGraveyardCardIndices;
        final List<UUID> fMultiTargetIds = multiTargetIds;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(idx, fXValue, fTargetId, fDamageAssignments,
                        fMultiTargetIds, null, null, fSacrificePermanentId,
                        null, null, null, null, null, fExileIndices, null, null, null)));
        if (hand.size() >= handSizeBefore) {
            Card failedCard = hand.size() > idx ? hand.get(idx) : null;
            log.warn("AI (Hard): Precombat priority cast failed for '{}' in game {}",
                    failedCard != null ? failedCard.getName() : "?", gameId);
            return false;
        }
        return true;
    }

    /**
     * Wraps sorcery-speed casting with "hold up mana" reasoning. Ranks <em>all</em>
     * castable instants and finds the optimal split between sorceries cast now and
     * instants held for later. For example, with 7 mana, a 3-cost sorcery, a 2-cost
     * instant, and a 3-cost counterspell, the AI can reason about casting the sorcery
     * while holding mana for one instant, or holding all 5 mana for both instants if
     * their combined value exceeds the sorcery.
     */
    private boolean tryCastSpellWithInstantAwareness(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Compute total value of ALL castable sorcery-speed spells (no reservation)
        double totalSorceryValue = estimateTotalCastableValue(gameData, hand, virtualPool, 0);

        // Evaluate all held instant candidates ranked by value descending
        List<HeldInstantCandidate> heldInstants = evaluateHeldInstants(gameData, hand, virtualPool);

        if (heldInstants.isEmpty()) {
            return tryCastSpellMCTS(gameData);
        }

        // Find the optimal split between sorceries and held instants.
        // For each number of instants to hold (1..N), compute:
        //   combined = sorcery value (with reserved mana) + cumulative instant held value
        // The instant values are discounted by 0.8 to bias toward casting now (uncertain
        // whether instants will actually be needed).
        double bestCombinedValue = totalSorceryValue; // baseline: hold 0 instants
        int bestReservedMana = 0;
        int bestInstantsHeld = 0;

        int cumulativeCost = 0;
        double cumulativeInstantValue = 0;

        for (int i = 0; i < heldInstants.size(); i++) {
            HeldInstantCandidate instant = heldInstants.get(i);
            cumulativeCost += instant.effectiveCost();
            cumulativeInstantValue += instant.heldValue();

            if (cumulativeCost > virtualPool.getTotal()) break;

            double sorceryValueWithReserve = estimateTotalCastableValue(
                    gameData, hand, virtualPool, cumulativeCost);
            // Discount held instant value to reflect uncertainty of casting them
            double combinedValue = sorceryValueWithReserve + cumulativeInstantValue * 0.8;

            if (combinedValue > bestCombinedValue) {
                bestCombinedValue = combinedValue;
                bestReservedMana = cumulativeCost;
                bestInstantsHeld = i + 1;
            }
        }

        if (bestInstantsHeld == 0) {
            return tryCastSpellMCTS(gameData);
        }

        // Check if we can still cast some sorceries while holding instant mana
        double sorceryWithReserve = estimateTotalCastableValue(
                gameData, hand, virtualPool, bestReservedMana);
        if (sorceryWithReserve > 0) {
            log.info("AI (Hard): Casting sorceries while reserving {} mana for {} instant(s) "
                            + "(reserve_value={}, instant_held={}) in game {}",
                    bestReservedMana, bestInstantsHeld,
                    String.format("%.1f", sorceryWithReserve),
                    String.format("%.1f", bestCombinedValue - sorceryWithReserve), gameId);
            return tryCastSpellMCTS(gameData);
        }

        log.info("AI (Hard): Holding mana for {} instant(s) (held={}, totalSorcery={}) in game {}",
                bestInstantsHeld,
                String.format("%.1f", bestCombinedValue),
                String.format("%.1f", totalSorceryValue), gameId);
        return false;
    }

    /** A held instant candidate with its timing-adjusted held value and effective mana cost. */
    private record HeldInstantCandidate(Card card, double heldValue, int effectiveCost) {}

    /**
     * Evaluates all castable instants in hand and returns them ranked by held value
     * descending. Each candidate includes the timing-adjusted held value (with category
     * multiplier) and the effective mana cost after cost modifiers. Used by both the
     * main-phase and precombat instant-holding logic to reason about multiple instants.
     */
    private List<HeldInstantCandidate> evaluateHeldInstants(GameData gameData, List<Card> hand,
                                                            ManaPool virtualPool) {
        List<HeldInstantCandidate> candidates = new ArrayList<>();
        for (Card card : hand) {
            if (!isInstantSpeedCard(card)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            InstantCategory category = card.hasType(CardType.INSTANT)
                    ? InstantCategoryClassifier.classify(card)
                    : InstantCategoryClassifier.classifyFlashCreature(card);

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
                case FLASH_CREATURE -> 1.2; // Holding for end-of-turn flash is valuable
                case OTHER -> 0.8;          // Slight discount for unknown timing
            };

            double heldValue = baseValue * multiplier;
            int modifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card);
            int effectiveCost = Math.max(0, card.getManaValue() + modifier);
            candidates.add(new HeldInstantCandidate(card, heldValue, effectiveCost));
        }

        candidates.sort(Comparator.comparingDouble(HeldInstantCandidate::heldValue).reversed());
        return candidates;
    }

    /**
     * Estimates the total value of all sorcery-speed spells the AI could cast this turn.
     * Uses a greedy approach: sorts spells by value descending and "casts" the most
     * valuable first until the mana budget is exhausted. Optionally reserves mana for
     * other uses (e.g. holding up instant mana).
     *
     * @param reservedMana mana to reserve (subtracted from available total)
     * @return total estimated value of castable spells within the budget
     */
    private double estimateTotalCastableValue(GameData gameData, List<Card> hand,
                                              ManaPool virtualPool, int reservedMana) {
        record SpellCandidate(Card card, double value) {}
        List<SpellCandidate> candidates = new ArrayList<>();

        for (Card card : hand) {
            if (card.hasType(CardType.LAND) || isInstantSpeedCard(card)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                candidates.add(new SpellCandidate(card, value));
            }
        }

        if (candidates.isEmpty()) return 0;

        // Greedy: cast the most valuable spells first
        candidates.sort(Comparator.comparingDouble(SpellCandidate::value).reversed());

        int availableMana = virtualPool.getTotal() - reservedMana;
        double totalValue = 0;
        int manaSpent = 0;

        for (SpellCandidate candidate : candidates) {
            int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), candidate.card);
            int effectiveCost = Math.max(0, candidate.card.getManaValue() + costModifier);
            if (manaSpent + effectiveCost <= availableMana) {
                totalValue += candidate.value;
                manaSpent += effectiveCost;
            }
        }

        return totalValue;
    }

    /**
     * Uses MCTS to decide which spell to cast (or whether to pass).
     * Falls back to evaluator-based logic for 0-1 castable options.
     */
    private boolean tryCastSpellMCTS(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Count castable spells (excludes instant-speed cards — those use timing evaluation)
        int castableCount = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND) || isInstantSpeedCard(card)) continue;
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
            if (isInstantSpeedCard(card)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                // Slight priority for card-draw ETB: cast before other spells
                // to draw cards (and gain information) for subsequent decisions
                if (hasCardDrawEtb(card)) {
                    value += 0.5;
                }
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

    /**
     * Returns true if the card can be cast at instant speed — either an actual Instant
     * or a card with flash (e.g. flash creatures).
     */
    private boolean isInstantSpeedCard(Card card) {
        return card.hasType(CardType.INSTANT) || card.getKeywords().contains(Keyword.FLASH);
    }

    // ===== Instant Casting with Timing Evaluation =====

    /**
     * Tries to cast the best instant-speed card (instants and flash creatures) using
     * category-based timing with value multipliers.
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
            if (!isInstantSpeedCard(card)) continue;
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;

            InstantCategory category = card.hasType(CardType.INSTANT)
                    ? InstantCategoryClassifier.classify(card)
                    : InstantCategoryClassifier.classifyFlashCreature(card);
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
            case FLASH_CREATURE -> isOpponentsTurn && step == TurnStep.END_STEP; // Dodge sorcery-speed removal
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
            case FLASH_CREATURE -> 1.3; // End-of-turn flash is a strong tempo play
            case OTHER -> 0.9;
        };
    }

    /**
     * Evaluates the value of countering a specific spell on the stack using effect-based
     * analysis. Instead of a flat mana-value score, runs SpellEvaluator from the opponent's
     * perspective so that board wipes threatening a full board, removal targeting our best
     * creature, and powerful ETB effects all score appropriately.
     *
     * Also applies a threat reservation threshold: when the AI has a strong board position,
     * it saves counterspells for high-impact spells rather than wasting them on mediocre threats.
     */
    private double evaluateCounterspellValue(GameData gameData, Card counterSpell, UUID spellTargetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard().getId().equals(spellTargetId)) {
                Card targetCard = entry.getCard();
                int aiLife = gameData.playerLifeTotals.getOrDefault(aiPlayer.getId(), 20);
                int counterManaValue = counterSpell.getManaValue();

                // Evaluate from the opponent's perspective — how much does this spell help them?
                // Board wipes score huge when the AI has a big board, removal scores high when
                // the AI has a valuable creature, etc.
                UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
                double value = spellEvaluator.estimateSpellValue(gameData, targetCard, opponentId);

                // Fallback: if the spell evaluator can't assess the spell (unknown/unhandled
                // effects), use mana value as a baseline so we still counter expensive mysteries
                if (value <= 0 && targetCard.getManaValue() > 0) {
                    value = targetCard.getManaValue() * 3.0;
                }

                // Mana efficiency gate: don't counter cheap spells unless they have high impact.
                // A Doom Blade (MV=2) targeting our Serra Angel has high impact and should be
                // countered even though it costs less than Cancel. But a Llanowar Elves (MV=1)
                // is low-impact and not worth a 3-mana counter.
                if (aiLife > 5 && targetCard.getManaValue() < counterManaValue && value < 15.0) {
                    return 0;
                }

                // Threat reservation: when the AI's board is strong, save counterspells for
                // high-impact spells. A 2/2 vanilla creature isn't worth countering when we
                // have a dominant board — but a board wipe absolutely is.
                if (aiLife > 5) {
                    List<Permanent> aiBattlefield = gameData.playerBattlefields
                            .getOrDefault(aiPlayer.getId(), List.of());
                    double aiBoardStrength = aiBattlefield.stream()
                            .filter(p -> gameQueryService.isCreature(gameData, p))
                            .mapToDouble(p -> boardEvaluator.creatureScore(
                                    gameData, p, aiPlayer.getId(), opponentId))
                            .sum();

                    // Stronger board = pickier about what to counter.
                    // At 30+ board strength, require value > 15; scales up with board strength.
                    if (aiBoardStrength > 30) {
                        double reservationThreshold = 15.0 + (aiBoardStrength - 30) * 0.3;
                        if (value < reservationThreshold) {
                            return 0;
                        }
                    }
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

        log.info("AI (Hard): Casting {} at instant speed{} (value={}) in game {}", card.getName(),
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
            log.warn("AI (Hard): Instant-speed cast failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    // ===== Activated Abilities =====

    /**
     * Tries to activate the best non-mana activated ability on any controlled permanent.
     * Checks timing restrictions, costs, targeting, and evaluates expected value.
     * Skips mana abilities (handled by AiManaManager) and loyalty abilities (complex).
     * Uses timing awareness to only activate combat-oriented abilities during combat
     * and card-draw abilities at end of opponent's turn.
     */
    private boolean tryActivateAbility(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        if (battlefield.isEmpty()) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        record AbilityCandidate(int permanentIndex, int abilityIndex, ActivatedAbility ability,
                                Permanent permanent, double value, UUID targetId) {}

        List<AbilityCandidate> candidates = new ArrayList<>();

        for (int permIdx = 0; permIdx < battlefield.size(); permIdx++) {
            Permanent permanent = battlefield.get(permIdx);

            // Build the full ability list matching AbilityActivationService's logic
            List<ActivatedAbility> abilities = buildEffectiveAbilityList(gameData, permanent);

            for (int abilIdx = 0; abilIdx < abilities.size(); abilIdx++) {
                ActivatedAbility ability = abilities.get(abilIdx);

                // Skip mana abilities (already handled by AiManaManager)
                if (isManaAbility(ability)) continue;

                // Skip loyalty abilities (complex planeswalker rules)
                if (ability.getLoyaltyCost() != null) continue;

                // Skip spell-targeting abilities (counterspell-type, too complex for v1)
                if (ability.isNeedsSpellTarget()) continue;

                // Skip multi-target abilities (rare for activated abilities)
                if (ability.isMultiTarget()) continue;

                // Basic activation checks
                if (!canActivateAbility(gameData, permanent, ability, abilIdx, virtualPool)) continue;

                // Timing-awareness: skip pump abilities outside combat, draw abilities outside end step
                if (!isGoodTimingForAbility(gameData, ability)) continue;

                // Evaluate value
                double value = spellEvaluator.evaluateAbilityEffects(
                        gameData, ability.getEffects(), aiPlayer.getId());
                value -= evaluateAbilityCosts(gameData, ability, permanent);
                if (value <= 0) continue;

                // Find target if needed
                UUID targetId = null;
                if (ability.isNeedsTarget()) {
                    targetId = targetSelector.chooseAbilityTarget(
                            gameData, ability, aiPlayer.getId(), permanent);
                    if (targetId == null) continue;
                }

                candidates.add(new AbilityCandidate(permIdx, abilIdx, ability,
                        permanent, value, targetId));
            }
        }

        if (candidates.isEmpty()) return false;

        candidates.sort(Comparator.comparingDouble(AbilityCandidate::value).reversed());
        AbilityCandidate best = candidates.getFirst();

        // Tap mana for the ability cost
        if (best.ability().getManaCost() != null) {
            manaManager.tapLandsForCost(gameData, aiPlayer.getId(),
                    best.ability().getManaCost(), 0, manaTapAction());
            if (gameData.interaction.isAwaitingInput()) {
                return true; // Mana ability triggered a pending choice
            }
        }

        log.info("AI (Hard): Activating ability {} on {} (value={}) in game {}",
                best.abilityIndex(), best.permanent().getCard().getName(),
                String.format("%.1f", best.value()), gameId);

        final int permIdx = best.permanentIndex();
        final int abilIdx = best.abilityIndex();
        final UUID finalTargetId = best.targetId();
        send(() -> messageHandler.handleActivateAbility(selfConnection,
                new ActivateAbilityRequest(permIdx, abilIdx, null, finalTargetId, null, null, null)));
        return true;
    }

    /**
     * Builds the complete list of activated abilities for a permanent, matching the
     * same logic as AbilityActivationService. Includes the permanent's own abilities,
     * abilities granted by static effects, and temporary abilities.
     */
    private List<ActivatedAbility> buildEffectiveAbilityList(GameData gameData, Permanent permanent) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities;
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            abilities = new ArrayList<>(staticBonus.grantedActivatedAbilities());
        } else {
            abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        return abilities;
    }

    /**
     * Returns true if an activated ability is a mana ability per CR 605.1a:
     * no target, no spell target, no loyalty cost, and has at least one mana-producing effect.
     */
    private static boolean isManaAbility(ActivatedAbility ability) {
        if (ability.isNeedsTarget() || ability.isNeedsSpellTarget() || ability.getLoyaltyCost() != null) {
            return false;
        }
        List<CardEffect> nonCostEffects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();
        return !nonCostEffects.isEmpty() && nonCostEffects.stream().anyMatch(e -> e instanceof ManaProducingEffect);
    }

    /**
     * Checks whether an activated ability can be activated: tap state, summoning sickness,
     * Arrest, timing restriction, per-turn limit, mana affordability, and cost effects.
     */
    private boolean canActivateAbility(GameData gameData, Permanent permanent,
                                       ActivatedAbility ability, int abilityIndex,
                                       ManaPool virtualPool) {
        // Tap check
        if (ability.isRequiresTap()) {
            if (permanent.isTapped()) return false;
            if (permanent.isSummoningSick()
                    && gameQueryService.isCreature(gameData, permanent)
                    && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
                return false;
            }
        }

        // Arrest check (enchanted creature can't activate abilities)
        if (gameQueryService.hasAuraWithEffect(gameData, permanent,
                EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            return false;
        }

        // Timing restriction
        if (!canMeetAbilityTimingRestriction(gameData, ability, permanent)) return false;

        // Per-turn activation limit
        if (isAbilityActivationLimitReached(gameData, permanent, abilityIndex, ability)) return false;

        // Required subtype count (e.g., "Activate only if you control five or more Vampires")
        if (ability.getRequiredControlledSubtype() != null) {
            int count = gameQueryService.countControlledSubtypePermanents(
                    gameData, aiPlayer.getId(), ability.getRequiredControlledSubtype());
            if (count < ability.getRequiredControlledSubtypeCount()) return false;
        }

        // Mana affordability
        if (ability.getManaCost() != null) {
            ManaCost cost = new ManaCost(ability.getManaCost());
            if (!cost.canPay(virtualPool, 0)) return false;
        }

        // Cost effects payability
        return canPayAbilityCostEffects(gameData, ability, permanent);
    }

    /**
     * Checks if the ability's timing restriction is met.
     */
    private boolean canMeetAbilityTimingRestriction(GameData gameData, ActivatedAbility ability,
                                                    Permanent permanent) {
        ActivationTimingRestriction restriction = ability.getTimingRestriction();
        if (restriction == null) return true;

        return switch (restriction) {
            case METALCRAFT -> gameQueryService.isMetalcraftMet(gameData, aiPlayer.getId());
            case MORBID -> gameQueryService.isMorbidMet(gameData);
            case ONLY_DURING_YOUR_UPKEEP -> aiPlayer.getId().equals(gameData.activePlayerId)
                    && gameData.currentStep == TurnStep.UPKEEP;
            case ONLY_WHILE_ATTACKING -> permanent.isAttacking();
            case ONLY_WHILE_CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case POWER_4_OR_GREATER -> gameQueryService.getEffectivePower(gameData, permanent) >= 4;
            case RAID -> gameData.playersDeclaredAttackersThisTurn.contains(aiPlayer.getId());
            case SORCERY_SPEED -> aiPlayer.getId().equals(gameData.activePlayerId)
                    && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                    && gameData.stack.isEmpty();
        };
    }

    /**
     * Returns true if the per-turn activation limit has been reached for this ability.
     */
    private boolean isAbilityActivationLimitReached(GameData gameData, Permanent permanent,
                                                    int abilityIndex, ActivatedAbility ability) {
        if (ability.getMaxActivationsPerTurn() == null) return false;
        Map<Integer, Integer> counts = gameData.activatedAbilityUsesThisTurn.get(permanent.getId());
        int current = counts != null ? counts.getOrDefault(abilityIndex, 0) : 0;
        return current >= ability.getMaxActivationsPerTurn();
    }

    /**
     * Checks whether the non-mana costs of an activated ability can be paid
     * (sacrifice, life, charge counters, etc.).
     */
    private boolean canPayAbilityCostEffects(GameData gameData, ActivatedAbility ability,
                                             Permanent permanent) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());

        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof SacrificeCreatureCost sacCost) {
                boolean hasCreature = battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .anyMatch(p -> !sacCost.excludeSelf() || !p.getId().equals(permanent.getId()));
                if (!hasCreature) return false;
            } else if (effect instanceof SacrificeArtifactCost) {
                boolean hasArtifact = battlefield.stream()
                        .anyMatch(p -> gameQueryService.isArtifact(gameData, p));
                if (!hasArtifact) return false;
            } else if (effect instanceof SacrificePermanentCost sacCost) {
                boolean hasMatch = battlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacCost.filter()));
                if (!hasMatch) return false;
            } else if (effect instanceof PayLifeCost lifeCost) {
                int life = gameData.getLife(aiPlayer.getId());
                if (life <= lifeCost.amount()) return false;
            } else if (effect instanceof RemoveChargeCountersFromSourceCost counterCost) {
                if (permanent.getChargeCounters() < counterCost.count()) return false;
            } else if (effect instanceof RemoveCounterFromSourceCost counterCost) {
                int available = switch (counterCost.counterType()) {
                    case PLUS_ONE_PLUS_ONE -> permanent.getPlusOnePlusOneCounters();
                    case CHARGE -> permanent.getChargeCounters();
                    default -> 0;
                };
                if (available < counterCost.count()) return false;
            }
        }
        return true;
    }

    /**
     * Estimates the cost of an ability's additional costs as a score deduction.
     * Higher cost means the ability needs higher effect value to be worth activating.
     */
    private double evaluateAbilityCosts(GameData gameData, ActivatedAbility ability,
                                        Permanent permanent) {
        double cost = 0;
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        BoardEvaluator boardEval = new BoardEvaluator(gameQueryService);

        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof SacrificeSelfCost) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    cost += boardEval.creatureScore(gameData, permanent, aiPlayer.getId(), opponentId);
                } else {
                    cost += permanent.getCard().getManaValue() * 3.0;
                }
            } else if (effect instanceof SacrificeCreatureCost) {
                // Deduct the value of the weakest creature we'd sacrifice
                List<Permanent> creatures = gameData.playerBattlefields
                        .getOrDefault(aiPlayer.getId(), List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .toList();
                double weakest = creatures.stream()
                        .mapToDouble(p -> boardEval.creatureScore(gameData, p, aiPlayer.getId(), opponentId))
                        .min()
                        .orElse(0);
                cost += weakest;
            } else if (effect instanceof PayLifeCost lifeCost) {
                cost += lifeCost.amount() * 1.5;
            } else if (effect instanceof RemoveChargeCountersFromSourceCost counterCost) {
                cost += counterCost.count();
            }
        }
        return cost;
    }

    /**
     * Determines whether the current game state is a good time to activate an ability
     * based on its effects. Pump abilities are best during combat, card draw is best
     * at opponent's end step, and most other abilities are fine at any priority.
     */
    private boolean isGoodTimingForAbility(GameData gameData, ActivatedAbility ability) {
        List<CardEffect> nonCostEffects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();

        boolean isPump = nonCostEffects.stream().anyMatch(e ->
                e instanceof BoostSelfEffect || e instanceof BoostTargetCreatureEffect);
        boolean isDraw = nonCostEffects.stream().anyMatch(e -> e instanceof DrawCardEffect);
        boolean isRegen = nonCostEffects.stream().anyMatch(e -> e instanceof RegenerateEffect);

        TurnStep step = gameData.currentStep;
        boolean isOpponentsTurn = !aiPlayer.getId().equals(gameData.activePlayerId);

        // Pump abilities: only during combat (declare blockers or combat damage)
        if (isPump) {
            return step == TurnStep.DECLARE_BLOCKERS || step == TurnStep.COMBAT_DAMAGE;
        }

        // Card draw abilities: opponent's end step (to keep mana open)
        if (isDraw && !isRegen) {
            return isOpponentsTurn && step == TurnStep.END_STEP;
        }

        // Regenerate: keep available — activate preemptively is fine at most times
        // but avoid main phase when we could be casting spells instead
        if (isRegen) {
            return step == TurnStep.DECLARE_BLOCKERS
                    || step == TurnStep.COMBAT_DAMAGE
                    || step == TurnStep.DECLARE_ATTACKERS;
        }

        // Sorcery-speed abilities are already gated by canMeetAbilityTimingRestriction
        // Everything else (damage, tap, tokens, etc.) can be used at any priority
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

        // Evaluate race state to adjust aggression
        RaceEvaluator.RaceState raceState = evaluateRace(gameData);

        // If winning the race, attack with all available creatures — trading is fine
        // because we'll kill the opponent first. Skip MCTS overhead in this case.
        if (raceState.aiWinningRace() && !raceState.aiLosingRace()) {
            log.info("AI (Hard): Winning the race (AI clock={}, opp clock={}), attacking aggressively in game {}",
                    raceState.aiClock(), raceState.opponentClock(), gameId);
            List<Integer> attackerIndices = new ArrayList<>(availableIndices);
            attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);
            attackerIndices = prepareAttackersForTax(gameData, attackerIndices);
            log.info("AI (Hard): Declaring {} aggressive attackers in game {}", attackerIndices.size(), gameId);
            final List<Integer> finalAttackerIndices = attackerIndices;
            send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                    new DeclareAttackersRequest(finalAttackerIndices, null)));
            return;
        }

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

        // Evaluate race state to inform blocking strategy
        RaceEvaluator.RaceState raceState = evaluateRace(gameData);

        if (raceState.aiWinningRace()) {
            // Winning the race: don't block small damage, preserve creatures for attacking.
            // Only block if incoming damage is lethal or if we get a favorable trade.
            int totalIncoming = 0;
            for (int idx : attackerIndices) {
                totalIncoming += gameQueryService.getEffectivePower(gameData, opponentBattlefield.get(idx));
            }
            int aiLife = gameData.getLife(aiPlayer.getId());

            if (totalIncoming < aiLife) {
                // Not lethal — skip blocking entirely to keep creatures alive for the race.
                // Exception: still do mandatory blocks (lure, must-block-if-able) via the simulator.
                log.info("AI (Hard): Winning race (AI clock={}, opp clock={}), skipping non-lethal block ({} dmg vs {} life) in game {}",
                        raceState.aiClock(), raceState.opponentClock(), totalIncoming, aiLife, gameId);
                // Still run exhaustive search to handle mandatory block constraints
                List<int[]> assignments = combatSimulator.findBestBlockersExhaustive(
                        gameData, aiPlayer.getId(), attackerIndices, blockerIndices);
                // Filter to only mandatory blocks (lure/must-block-if-able)
                List<int[]> mandatoryOnly = filterToMandatoryBlocks(gameData, assignments, opponentBattlefield);
                List<BlockerAssignment> blockerAssignments = mandatoryOnly.stream()
                        .map(a -> new BlockerAssignment(a[0], a[1]))
                        .toList();
                log.info("AI (Hard): Declaring {} mandatory-only blockers in game {}", blockerAssignments.size(), gameId);
                sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
                return;
            }
        }

        // Losing the race or neutral — use exhaustive search which already handles
        // lethal-incoming chump blocking logic via CombatSimulator
        if (raceState.aiLosingRace()) {
            log.info("AI (Hard): Losing race (AI clock={}, opp clock={}), blocking defensively in game {}",
                    raceState.aiClock(), raceState.opponentClock(), gameId);
        }

        List<int[]> assignments = combatSimulator.findBestBlockersExhaustive(
                gameData, aiPlayer.getId(), attackerIndices, blockerIndices);

        List<BlockerAssignment> blockerAssignments = assignments.stream()
                .map(a -> new BlockerAssignment(a[0], a[1]))
                .toList();

        log.info("AI (Hard): Declaring {} blockers (exhaustive search) in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
    }

    /**
     * Filters blocker assignments to only include mandatory blocks: those blocking
     * attackers with lure ("must be blocked by all creatures") or "must be blocked
     * if able" effects. Used when winning the race to avoid unnecessary blocking.
     */
    private List<int[]> filterToMandatoryBlocks(GameData gameData, List<int[]> assignments,
                                                 List<Permanent> opponentBattlefield) {
        List<int[]> mandatory = new ArrayList<>();
        for (int[] assignment : assignments) {
            int attackerIdx = assignment[1];
            Permanent attacker = opponentBattlefield.get(attackerIdx);
            if (hasMandatoryBlockRequirement(gameData, attacker)) {
                mandatory.add(assignment);
            }
        }
        return mandatory;
    }

    /**
     * Returns true if the attacker has a lure or must-be-blocked-if-able effect.
     */
    private boolean hasMandatoryBlockRequirement(GameData gameData, Permanent attacker) {
        // Check for "must be blocked by all creatures" (Lure effects)
        boolean hasLure = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect.class::isInstance);
        if (hasLure) return true;
        if (gameQueryService.hasAuraWithEffect(gameData, attacker,
                com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect.class)) {
            return true;
        }
        // Check for "must be blocked if able"
        if (attacker.isMustBeBlockedThisTurn()) return true;
        boolean hasMustBlock = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect.class::isInstance);
        if (hasMustBlock) return true;
        return gameQueryService.hasAuraWithEffect(gameData, attacker,
                com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect.class);
    }

    // ===== Card Choice (context-aware discard) =====

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) return;

        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        // Use context-aware evaluation that considers board state, removal needs,
        // mana development, castability, and redundancy
        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.evaluateCardForDiscard(gameData, hand.get(i), hand, aiPlayer.getId())))
                .orElse(validIndices.iterator().next());

        log.info("AI (Hard): Discarding card at index {} ({}) in game {}",
                bestIndex, hand.get(bestIndex).getName(), gameId);
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

    /**
     * Returns true if the card has a {@link DrawCardEffect} in its
     * enter-the-battlefield effects (useful for prioritizing draw-first casting).
     */
    private boolean hasCardDrawEtb(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof DrawCardEffect) {
                return true;
            }
        }
        return false;
    }

    // ===== Race Evaluation & Burn-to-Face Lethal =====

    /**
     * Checks if the AI can deal lethal damage to the opponent right now using only
     * burn spells in hand. If so, casts burn spells targeting the opponent's face
     * until they're dead. This is checked before combat — no point attacking if we
     * can just burn them out.
     */
    private boolean tryBurnToFaceLethal(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Collect castable burn spells that can target a player
        List<Card> castableBurn = new ArrayList<>();
        for (Card card : hand) {
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            if (raceEvaluator.getBurnToFaceDamage(card) > 0) {
                castableBurn.add(card);
            }
        }

        if (castableBurn.isEmpty()) return false;

        RaceEvaluator.RaceState raceState = raceEvaluator.evaluate(gameData, aiPlayer.getId(), castableBurn);
        if (!raceState.burnLethal()) return false;

        // We have lethal burn! Cast the most efficient burn spells (highest damage first)
        // until the opponent is dead.
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        int opponentLife = gameData.getLife(opponentId);

        castableBurn.sort(Comparator.comparingInt(raceEvaluator::getBurnToFaceDamage).reversed());

        int damageDealt = 0;
        for (Card burnCard : castableBurn) {
            if (damageDealt >= opponentLife) break;

            int cardIndex = hand.indexOf(burnCard);
            if (cardIndex < 0) continue;

            log.info("AI (Hard): Burn-to-face lethal! Casting {} for {} damage in game {}",
                    burnCard.getName(), raceEvaluator.getBurnToFaceDamage(burnCard), gameId);

            // Cast the burn spell targeting the opponent player
            if (tapManaForSpell(gameData, burnCard, null, 0)) {
                return true; // Mana ability triggered a pending choice
            }
            int handSizeBefore = hand.size();
            final int idx = cardIndex;
            final UUID targetId = opponentId;
            send(() -> messageHandler.handlePlayCard(selfConnection,
                    new PlayCardRequest(idx, null, targetId, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
            if (hand.size() >= handSizeBefore) {
                log.warn("AI (Hard): Burn-to-face lethal cast failed in game {}", gameId);
                return false;
            }

            damageDealt += raceEvaluator.getBurnToFaceDamage(burnCard);
            return true; // Cast one spell at a time; will re-enter handleGameState
        }

        return false;
    }

    /**
     * Returns the current race state for use by combat decisions.
     */
    private RaceEvaluator.RaceState evaluateRace(GameData gameData) {
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        List<Card> castableBurn = new ArrayList<>();
        for (Card card : hand) {
            if (card.getManaCost() == null) continue;
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            if (raceEvaluator.getBurnToFaceDamage(card) > 0) {
                castableBurn.add(card);
            }
        }

        return raceEvaluator.evaluate(gameData, aiPlayer.getId(), castableBurn);
    }
}
