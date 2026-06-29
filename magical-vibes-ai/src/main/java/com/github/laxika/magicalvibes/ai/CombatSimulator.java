package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Exhaustive combat search with pure arithmetic (no state mutation).
 * Simulates combat outcomes to find the best set of attackers or blockers.
 */
public class CombatSimulator {

    private static final int MAX_ATTACKER_SUBSET_BITS = 12;
    private static final long MAX_BLOCKER_SEARCH_SPACE = 2_000_000L;

    /**
     * Life threshold below which the defensive value penalty kicks in. At life totals
     * at or above this, we're not in "losing the race" territory, so attacking with
     * non-vigilance creatures isn't penalized. Chosen to comfortably cover the standard
     * 20-starting-life danger zone while still letting the penalty engage before things
     * turn critical.
     */
    private static final int DEFENSIVE_PENALTY_LIFE_THRESHOLD = 15;

    private final GameQueryService gameQueryService;
    private final BoardEvaluator boardEvaluator;

    public CombatSimulator(GameQueryService gameQueryService, BoardEvaluator boardEvaluator) {
        this.gameQueryService = gameQueryService;
        this.boardEvaluator = boardEvaluator;
    }

    record CreatureInfo(int index, UUID id, Permanent perm, int power, int toughness,
                        boolean flying, boolean firstStrike, boolean doubleStrike,
                        boolean trample, boolean lifelink, boolean indestructible,
                        boolean menace, boolean fear, boolean intimidate, boolean reach, boolean defender,
                        boolean cantBeBlocked, boolean isArtifact, boolean infect,
                        CardColor color, double creatureScore) {}

    record CombatOutcome(int aiLifeChange, int opponentLifeChange, int opponentPoisonChange,
                         double aiCreaturesLostValue, double opponentCreaturesLostValue) {
        double evaluationDelta() {
            return -opponentLifeChange * 2.0
                    + opponentPoisonChange * 4.0
                    + opponentCreaturesLostValue
                    - aiCreaturesLostValue
                    + aiLifeChange * 0.5;
        }
    }

    /**
     * Finds the best set of attackers among available creatures.
     * Must-attack creatures (from "attacks each combat if able" effects) are always included.
     */
    public List<Integer> findBestAttackers(GameData gameData, UUID aiPlayerId,
                                           List<Integer> availableAttackerIndices,
                                           List<Integer> mustAttackIndices) {
        return findBestAttackers(gameData, aiPlayerId, availableAttackerIndices,
                mustAttackIndices, OpponentThreatEstimator.ThreatEstimate.NONE);
    }

    /**
     * Finds the best set of attackers, applying a pessimism modifier based on the
     * opponent's estimated combat trick threat. When the opponent has untapped mana
     * and cards in hand, attacks that are vulnerable to pump spells are penalized.
     */
    public List<Integer> findBestAttackers(GameData gameData, UUID aiPlayerId,
                                           List<Integer> availableAttackerIndices,
                                           List<Integer> mustAttackIndices,
                                           OpponentThreatEstimator.ThreatEstimate threatEstimate) {
        if (availableAttackerIndices.isEmpty()) {
            return List.of();
        }

        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        int opponentLife = gameData.getLife(opponentId);
        int opponentPoison = gameData.playerPoisonCounters.getOrDefault(opponentId, 0);
        int aiLife = gameData.getLife(aiPlayerId);

        // Build creature info for available attackers
        Set<Integer> mustAttackSet = new HashSet<>(mustAttackIndices);
        List<CreatureInfo> forcedAttackerInfos = new ArrayList<>();
        List<CreatureInfo> optionalAttackerInfos = new ArrayList<>();
        for (int idx : availableAttackerIndices) {
            Permanent perm = aiBattlefield.get(idx);
            CreatureInfo info = buildCreatureInfo(gameData, perm, idx, aiPlayerId, opponentId, oppBattlefield);
            if (mustAttackSet.contains(idx)) {
                forcedAttackerInfos.add(info);
            } else {
                optionalAttackerInfos.add(info);
            }
        }

        // Build creature info for potential blockers
        List<CreatureInfo> blockerInfos = new ArrayList<>();
        for (int i = 0; i < oppBattlefield.size(); i++) {
            Permanent perm = oppBattlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            blockerInfos.add(buildCreatureInfo(gameData, perm, i, opponentId, aiPlayerId));
        }

        // ===== Defensive value context =====
        // Preserving blockers matters when the opponent has a board that can counter-attack
        // AND we're under real pressure. We gate the analysis on a simple life threshold:
        // when the AI is at 15+ life, a few points of extra damage from a sub-optimal
        // attack don't change the game plan, and the penalty's greedy counter-attack
        // model (which ignores the attackers' own removal of opponents in combat) can
        // actively mis-evaluate trades in MCTS rollouts. The original motivation is
        // "losing the race with your last blockers", which only applies when life is low.
        // This cheap gate also keeps the MCTS rollout hot path free of any extra work
        // at typical starting-life totals.
        List<CreatureInfo> opponentNextTurnAttackers = List.of();
        List<CreatureInfo> aiPotentialBlockers = List.of();
        DefensiveBaseline defensiveBaseline = null;
        if (aiLife > 0 && aiLife < DEFENSIVE_PENALTY_LIFE_THRESHOLD) {
            opponentNextTurnAttackers = buildOpponentNextTurnAttackers(
                    gameData, aiPlayerId, opponentId, oppBattlefield, aiBattlefield);
            if (!opponentNextTurnAttackers.isEmpty()) {
                aiPotentialBlockers = buildAiPotentialBlockers(
                        gameData, aiPlayerId, opponentId, aiBattlefield);
                double[] baselineOutcome = estimateCounterAttackOutcome(
                        gameData, opponentNextTurnAttackers, aiPotentialBlockers, aiLife);
                defensiveBaseline = new DefensiveBaseline(baselineOutcome[0], baselineOutcome[1]);
            }
        }

        // Limit optional attackers to top creatures by score if too many
        if (optionalAttackerInfos.size() > MAX_ATTACKER_SUBSET_BITS) {
            optionalAttackerInfos.sort(Comparator.comparingDouble(CreatureInfo::creatureScore).reversed());
            optionalAttackerInfos = new ArrayList<>(optionalAttackerInfos.subList(0, MAX_ATTACKER_SUBSET_BITS));
        }

        // If all attackers are forced, return them all
        if (optionalAttackerInfos.isEmpty()) {
            return forcedAttackerInfos.stream().map(CreatureInfo::index).toList();
        }

        int n = optionalAttackerInfos.size();
        int totalSubsets = 1 << n;

        // Baseline: forced attackers only (score must beat this; if no must-attack, baseline is 0 = no attack)
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Integer> bestSubset = List.of();
        if (!forcedAttackerInfos.isEmpty()) {
            // Evaluate forced-only attack as baseline
            CombatOutcome forcedOutcome = simulateCombat(gameData, forcedAttackerInfos, blockerInfos, opponentLife);
            bestScore = forcedOutcome.evaluationDelta();
            bestSubset = forcedAttackerInfos.stream().map(CreatureInfo::index).toList();
        } else {
            bestScore = 0; // Not attacking at all scores 0
        }

        for (int mask = 0; mask < totalSubsets; mask++) {
            // Build subset: forced attackers + selected optional attackers
            List<CreatureInfo> subset = new ArrayList<>(forcedAttackerInfos);
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(optionalAttackerInfos.get(i));
                }
            }

            // Skip empty subset (no optional + no forced)
            if (subset.isEmpty()) continue;

            // Quick lethal check: if unblockable damage >= opponent life (or poison), pick immediately
            List<CreatureInfo> unblockable = subset.stream()
                    .filter(a -> a.cantBeBlocked || !canBeBlockedByAny(gameData, a, blockerInfos))
                    .toList();
            int unblockableLifeDamage = unblockable.stream()
                    .filter(a -> !a.infect)
                    .mapToInt(CreatureInfo::power)
                    .sum();
            int unblockablePoisonDamage = unblockable.stream()
                    .filter(a -> a.infect)
                    .mapToInt(CreatureInfo::power)
                    .sum();
            if (unblockableLifeDamage >= opponentLife
                    || unblockablePoisonDamage + opponentPoison >= 10) {
                return subset.stream().map(CreatureInfo::index).toList();
            }

            // Simulate greedy-optimal blocking by opponent
            CombatOutcome outcome = simulateCombat(gameData, subset, blockerInfos, opponentLife);
            double score = outcome.evaluationDelta();

            // Apply pessimism for opponent's potential combat tricks
            if (threatEstimate.hasThreat()) {
                score -= computeAttackTrickRisk(gameData, subset, blockerInfos, threatEstimate);
            }

            // Apply defensive value penalty: attacking taps non-vigilance creatures,
            // leaving them unable to block the opponent's counter-attack. When the
            // opponent has a significant board, sending creatures into a losing race
            // is worse than holding them back.
            if (defensiveBaseline != null) {
                score -= computeDefensiveValuePenalty(gameData, subset, aiPotentialBlockers,
                        opponentNextTurnAttackers, defensiveBaseline, aiLife);
            }

            if (score > bestScore) {
                bestScore = score;
                bestSubset = subset.stream().map(CreatureInfo::index).toList();
            }
        }

        return bestSubset;
    }

    /**
     * Finds the best blocker assignments for the AI as defender.
     *
     * <p>Single attacker-centric pass: each attacker's required block count is computed up
     * front from its combat flags (lure, mustBlockIfAble, menace) and atomically allocated
     * from the remaining candidate pool. A menace attacker's count must be 0 or ≥ 2 — if
     * fewer than 2 candidates exist, no blocker is "able to block" (CR 509.1a + 702.110b),
     * so the attacker is skipped.
     */
    public List<int[]> findBestBlockers(GameData gameData, UUID aiPlayerId,
                                        List<Integer> attackerIndices,
                                        List<Integer> availableBlockerIndices) {
        if (attackerIndices.isEmpty() || availableBlockerIndices.isEmpty()) {
            return List.of();
        }

        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        int aiLife = gameData.getLife(aiPlayerId);

        List<CreatureInfo> attackerInfos = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent perm = oppBattlefield.get(idx);
            attackerInfos.add(buildCreatureInfo(gameData, perm, idx, opponentId, aiPlayerId, aiBattlefield));
        }

        List<CreatureInfo> blockerInfos = new ArrayList<>();
        for (int idx : availableBlockerIndices) {
            Permanent perm = aiBattlefield.get(idx);
            blockerInfos.add(buildCreatureInfo(gameData, perm, idx, aiPlayerId, opponentId));
        }

        // Priority: lure first (forces all able), then mustBlockIfAble, then regular by threat desc.
        // Within lures, menace+lure sorts ahead so it claims a legal 2+ pool before a non-menace
        // lure can drain candidates.
        List<CreatureInfo> sortedAttackers = new ArrayList<>(attackerInfos);
        sortedAttackers.sort((a, b) -> {
            int lureCmp = Boolean.compare(hasLureEffect(gameData, b.perm), hasLureEffect(gameData, a.perm));
            if (lureCmp != 0) return lureCmp;
            int menaceCmp = Boolean.compare(b.menace, a.menace);
            if (menaceCmp != 0) return menaceCmp;
            int mbCmp = Boolean.compare(hasMustBeBlockedIfAbleEffect(gameData, b.perm),
                                        hasMustBeBlockedIfAbleEffect(gameData, a.perm));
            if (mbCmp != 0) return mbCmp;
            return Double.compare(b.creatureScore, a.creatureScore);
        });

        int totalIncoming = attackerInfos.stream().mapToInt(CreatureInfo::power).sum();
        boolean[] blockerUsed = new boolean[aiBattlefield.size()];
        List<int[]> assignments = new ArrayList<>();

        for (CreatureInfo attacker : sortedAttackers) {
            if (attacker.cantBeBlocked) continue;

            List<CreatureInfo> candidates = blockerInfos.stream()
                    .filter(b -> !blockerUsed[b.index])
                    .filter(b -> canBlock(gameData, b, attacker))
                    .toList();
            if (candidates.isEmpty()) continue;

            // Menace: no creature is "able to block" alone, so with <2 candidates skip entirely.
            if (attacker.menace && candidates.size() < 2) continue;

            boolean lure = hasLureEffect(gameData, attacker.perm);
            boolean mustBlock = hasMustBeBlockedIfAbleEffect(gameData, attacker.perm);
            boolean lethalIncoming = totalIncoming >= aiLife;

            List<CreatureInfo> chosen;
            if (lure) {
                // Every creature able to block must block — assign all candidates.
                chosen = new ArrayList<>(candidates);
            } else if (mustBlock) {
                // At least 1 required; menace bumps to 2.
                int needed = attacker.menace ? 2 : 1;
                chosen = attacker.menace
                        ? pickMenaceBlockers(attacker, candidates, lethalIncoming)
                        : List.of(pickBestSingleBlocker(attacker, candidates));
                if (chosen.size() < needed) {
                    // Menace lookup didn't find a favorable or chump pair — force the
                    // cheapest pair so the mandatory block still happens.
                    chosen = forceCheapestPair(candidates);
                }
            } else if (attacker.menace) {
                // Voluntary menace block: only worth it with a favorable pair, or chump-lethal.
                chosen = pickMenaceBlockers(attacker, candidates, lethalIncoming);
            } else {
                // Voluntary single block: take the best single blocker if favorable or lethal.
                CreatureInfo best = pickBestSingleBlocker(attacker, candidates);
                double bestValue = best != null
                        ? (attacker.trample ? evaluateTrampleBlock(attacker, best) : evaluateBlock(attacker, best))
                        : Double.NEGATIVE_INFINITY;
                chosen = (best != null && (bestValue > 0 || lethalIncoming)) ? List.of(best) : List.of();
            }

            if (chosen.isEmpty()) continue;

            for (CreatureInfo b : chosen) {
                assignments.add(new int[]{b.index, attacker.index});
                blockerUsed[b.index] = true;
            }

            // Update incoming damage estimate. For a single non-trample blocker, all damage
            // is stopped; for a single trample blocker, excess still lands. 2+ blockers
            // stop all damage (defender assigns damage; even chump pairs absorb it).
            if (attacker.trample && chosen.size() == 1) {
                int stopped = Math.min(attacker.power, chosen.get(0).toughness);
                totalIncoming -= stopped;
            } else {
                totalIncoming -= attacker.power;
            }

            // Trample soakers: if a single blocker isn't enough and damage is still lethal,
            // pile on more blockers to shrink the trample excess.
            if (attacker.trample && chosen.size() == 1 && totalIncoming >= aiLife) {
                int trampleExcess = Math.max(0, attacker.power - chosen.get(0).toughness);
                while (trampleExcess > 0 && totalIncoming >= aiLife) {
                    final int currentExcess = trampleExcess;
                    CreatureInfo bestAdditional = blockerInfos.stream()
                            .filter(b -> !blockerUsed[b.index])
                            .filter(b -> canBlock(gameData, b, attacker))
                            .max(Comparator.comparingInt((CreatureInfo b) -> Math.min(currentExcess, b.toughness))
                                    .thenComparingDouble(b -> -b.creatureScore))
                            .orElse(null);
                    if (bestAdditional == null) break;
                    int reduction = Math.min(trampleExcess, bestAdditional.toughness);
                    assignments.add(new int[]{bestAdditional.index, attacker.index});
                    blockerUsed[bestAdditional.index] = true;
                    trampleExcess -= reduction;
                    totalIncoming -= reduction;
                }
            }
        }

        return assignments;
    }

    private List<CreatureInfo> pickMenaceBlockers(CreatureInfo attacker, List<CreatureInfo> candidates,
                                                  boolean lethalIncoming) {
        if (candidates.size() < 2) return List.of();
        int[] bestPair = findBestBlockerPairForMenace(attacker, candidates, null);
        if (bestPair != null) {
            CreatureInfo a = findByIndex(candidates, bestPair[0]);
            CreatureInfo b = findByIndex(candidates, bestPair[1]);
            if (a != null && b != null) return List.of(a, b);
        }
        if (lethalIncoming) {
            List<CreatureInfo> sorted = candidates.stream()
                    .sorted(Comparator.comparingDouble(CreatureInfo::creatureScore))
                    .toList();
            return List.of(sorted.get(0), sorted.get(1));
        }
        return List.of();
    }

    private List<CreatureInfo> forceCheapestPair(List<CreatureInfo> candidates) {
        if (candidates.size() < 2) return List.of();
        List<CreatureInfo> sorted = candidates.stream()
                .sorted(Comparator.comparingDouble(CreatureInfo::creatureScore))
                .toList();
        return List.of(sorted.get(0), sorted.get(1));
    }

    private CreatureInfo pickBestSingleBlocker(CreatureInfo attacker, List<CreatureInfo> candidates) {
        CreatureInfo best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (CreatureInfo blocker : candidates) {
            double value = attacker.trample
                    ? evaluateTrampleBlock(attacker, blocker)
                    : evaluateBlock(attacker, blocker);
            if (value > bestValue) {
                bestValue = value;
                best = blocker;
            }
        }
        return best;
    }

    private static CreatureInfo findByIndex(List<CreatureInfo> list, int index) {
        for (CreatureInfo c : list) {
            if (c.index == index) return c;
        }
        return null;
    }

    /**
     * Finds optimal blocker assignments by exhaustively searching all legal combinations
     * when the search space is manageable. Falls back to the greedy heuristic for
     * large combat scenarios. Handles lure and must-block-if-able constraints first,
     * then optimizes remaining free blockers via enumeration.
     */
    public List<int[]> findBestBlockersExhaustive(GameData gameData, UUID aiPlayerId,
                                                   List<Integer> attackerIndices,
                                                   List<Integer> availableBlockerIndices) {
        return findBestBlockersExhaustive(gameData, aiPlayerId, attackerIndices,
                availableBlockerIndices, OpponentThreatEstimator.ThreatEstimate.NONE);
    }

    /**
     * Finds the best blocker assignments, applying a pessimism modifier based on the
     * opponent's estimated combat trick threat. When the opponent has untapped mana
     * and cards in hand, block configurations that would be flipped by a pump spell
     * (e.g. "3/3 blocking 2/3" becoming a disaster if the attacker gets +3/+3) are
     * penalized.
     */
    public List<int[]> findBestBlockersExhaustive(GameData gameData, UUID aiPlayerId,
                                                   List<Integer> attackerIndices,
                                                   List<Integer> availableBlockerIndices,
                                                   OpponentThreatEstimator.ThreatEstimate threatEstimate) {
        if (attackerIndices.isEmpty() || availableBlockerIndices.isEmpty()) {
            return List.of();
        }

        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        int aiLife = gameData.getLife(aiPlayerId);
        int aiPoison = gameData.playerPoisonCounters.getOrDefault(aiPlayerId, 0);

        List<CreatureInfo> attackerInfos = new ArrayList<>();
        for (int idx : attackerIndices) {
            attackerInfos.add(buildCreatureInfo(gameData, oppBattlefield.get(idx), idx, opponentId, aiPlayerId, aiBattlefield));
        }
        attackerInfos.sort(Comparator.comparingDouble(CreatureInfo::creatureScore).reversed());

        List<CreatureInfo> blockerInfos = new ArrayList<>();
        for (int idx : availableBlockerIndices) {
            blockerInfos.add(buildCreatureInfo(gameData, aiBattlefield.get(idx), idx, aiPlayerId, opponentId));
        }

        // Per-attacker blocker lists (forced assignments from constraint phases)
        List<List<CreatureInfo>> forcedAssignments = new ArrayList<>();
        for (int i = 0; i < attackerInfos.size(); i++) {
            forcedAssignments.add(new ArrayList<>());
        }
        boolean[] blockerUsed = new boolean[aiBattlefield.size()];

        // Sort lure attackers so menace+lure comes first: a menace+lure attacker requires
        // exactly its candidate pool size >= 2, and a non-menace lure that would drain the
        // pool first could leave the menace one in an illegal 1-blocker state.
        List<Integer> lureAttackerOrder = new ArrayList<>();
        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            if (hasLureEffect(gameData, attackerInfos.get(ai).perm)) lureAttackerOrder.add(ai);
        }
        lureAttackerOrder.sort((a, b) -> Boolean.compare(attackerInfos.get(b).menace, attackerInfos.get(a).menace));

        // Phase 1: Lure — every blocker able to block a lure attacker must do so.
        // Menace exception (CR 509.1a + 702.110b): a creature can't be "able to block"
        // a menace attacker unless another can legally join, so with <2 candidates the
        // attacker goes unblocked.
        for (int ai : lureAttackerOrder) {
            CreatureInfo lureAttacker = attackerInfos.get(ai);
            List<CreatureInfo> candidates = new ArrayList<>();
            for (CreatureInfo blocker : blockerInfos) {
                if (blockerUsed[blocker.index]) continue;
                if (!canBlock(gameData, blocker, lureAttacker)) continue;
                candidates.add(blocker);
            }
            if (lureAttacker.menace && candidates.size() < 2) continue;
            for (CreatureInfo blocker : candidates) {
                forcedAssignments.get(ai).add(blocker);
                blockerUsed[blocker.index] = true;
            }
        }

        // Phase 1b: Must-block-if-able — at least one blocker per such attacker, or two
        // if the attacker has menace. If fewer than the required count is available, no
        // creature is "able to block" so the attacker goes unblocked.
        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            CreatureInfo mustBlockAttacker = attackerInfos.get(ai);
            if (!hasMustBeBlockedIfAbleEffect(gameData, mustBlockAttacker.perm)) continue;
            if (!forcedAssignments.get(ai).isEmpty()) continue;

            int needed = mustBlockAttacker.menace ? 2 : 1;
            List<CreatureInfo> scored = new ArrayList<>();
            for (CreatureInfo blocker : blockerInfos) {
                if (blockerUsed[blocker.index]) continue;
                if (!canBlock(gameData, blocker, mustBlockAttacker)) continue;
                scored.add(blocker);
            }
            if (scored.size() < needed) continue;
            scored.sort(Comparator.comparingDouble((CreatureInfo b) -> evaluateBlock(mustBlockAttacker, b)).reversed());
            for (int i = 0; i < needed; i++) {
                CreatureInfo blocker = scored.get(i);
                forcedAssignments.get(ai).add(blocker);
                blockerUsed[blocker.index] = true;
            }
        }

        // Collect free blockers that have at least one legal target
        List<CreatureInfo> freeBlockers = new ArrayList<>();
        List<List<Integer>> legalTargets = new ArrayList<>();
        for (CreatureInfo blocker : blockerInfos) {
            if (blockerUsed[blocker.index]) continue;
            List<Integer> targets = new ArrayList<>();
            for (int ai = 0; ai < attackerInfos.size(); ai++) {
                CreatureInfo attacker = attackerInfos.get(ai);
                if (!attacker.cantBeBlocked && canBlock(gameData, blocker, attacker)) {
                    targets.add(ai);
                }
            }
            if (!targets.isEmpty()) {
                freeBlockers.add(blocker);
                legalTargets.add(targets);
            }
        }

        if (freeBlockers.isEmpty()) {
            return convertToAssignmentList(attackerInfos, forcedAssignments);
        }

        // Check search space; fall back to greedy if too large
        long searchSpace = 1;
        for (List<Integer> targets : legalTargets) {
            searchSpace *= (targets.size() + 1);
            if (searchSpace > MAX_BLOCKER_SEARCH_SPACE) {
                return findBestBlockers(gameData, aiPlayerId, attackerIndices, availableBlockerIndices);
            }
        }

        // Exhaustive enumeration with mixed-radix counter
        // choices[i] = 0 means "don't block", 1..k means block legalTargets[i][choices[i]-1]
        int n = freeBlockers.size();
        int[] choices = new int[n];
        int[] bestChoices = new int[n];
        int[] forcedSizes = new int[attackerInfos.size()];
        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            forcedSizes[ai] = forcedAssignments.get(ai).size();
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        // Record at least one valid fallback so we never return the empty-block default
        // if the initial best-score search yields no improvement (e.g. every non-forced
        // state is menace-invalid).
        boolean haveValidChoice = false;

        do {
            // Add free blocker choices to the assignment map
            for (int bi = 0; bi < n; bi++) {
                if (choices[bi] > 0) {
                    int ai = legalTargets.get(bi).get(choices[bi] - 1);
                    forcedAssignments.get(ai).add(freeBlockers.get(bi));
                }
            }

            // CSP validity: a menace attacker must have 0 or ≥2 blockers. Reject invalid
            // candidate states rather than scoring them — this keeps the search from
            // returning an illegal declaration the server will refuse.
            if (isValidBlockerAssignment(attackerInfos, forcedAssignments)) {
                double score = evaluateDefenderCombat(attackerInfos, forcedAssignments, aiLife, aiPoison);

                // Apply pessimism for opponent's potential combat tricks: a block that
                // looks profitable now could flip to a disaster if the opponent pumps an
                // attacker (e.g. 3/3 blocking 2/3 becomes a blocker loss after Giant Growth).
                if (threatEstimate.hasThreat()) {
                    score -= computeBlockTrickRisk(attackerInfos, forcedAssignments, aiLife, aiPoison, threatEstimate);
                }

                if (score > bestScore) {
                    bestScore = score;
                    System.arraycopy(choices, 0, bestChoices, 0, n);
                    haveValidChoice = true;
                }
            }

            // Reset lists back to forced-only
            for (int ai = 0; ai < attackerInfos.size(); ai++) {
                List<CreatureInfo> list = forcedAssignments.get(ai);
                while (list.size() > forcedSizes[ai]) {
                    list.removeLast();
                }
            }
        } while (incrementMixedRadix(choices, legalTargets));

        // If every non-forced state was invalid (e.g. the only free blocker creates a menace
        // 1-blocker violation for every attacker), fall back to the all-zero "don't block"
        // free-blocker configuration, which is always valid given valid forced assignments.
        if (!haveValidChoice) {
            java.util.Arrays.fill(bestChoices, 0);
        }

        // Apply best choices
        for (int bi = 0; bi < n; bi++) {
            if (bestChoices[bi] > 0) {
                int ai = legalTargets.get(bi).get(bestChoices[bi] - 1);
                forcedAssignments.get(ai).add(freeBlockers.get(bi));
            }
        }

        return convertToAssignmentList(attackerInfos, forcedAssignments);
    }

    /**
     * Evaluates a specific set of blocker assignments from the defender's perspective.
     * Higher score = better for the defender.
     */
    double evaluateDefenderCombat(List<CreatureInfo> attackerInfos,
                                          List<List<CreatureInfo>> blockAssignments,
                                          int aiLife, int aiPoison) {
        double defenderLifeLost = 0;
        double defenderPoisonGained = 0;
        double defenderLifeGained = 0;
        double defenderCreaturesLostValue = 0;
        double attackerCreaturesLostValue = 0;

        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            CreatureInfo attacker = attackerInfos.get(ai);
            List<CreatureInfo> blockers = blockAssignments.get(ai);

            // Menace requires 2+ blockers; fewer means effectively unblocked
            if (attacker.menace && blockers.size() < 2) {
                blockers = List.of();
            }

            if (blockers.isEmpty()) {
                if (attacker.infect) {
                    defenderPoisonGained += attacker.power;
                } else {
                    defenderLifeLost += attacker.power;
                }
                continue;
            }

            // Defender controls damage assignment order — sacrifice cheapest first
            List<CreatureInfo> orderedBlockers = new ArrayList<>(blockers);
            orderedBlockers.sort(Comparator.comparingDouble(CreatureInfo::creatureScore));

            int attackerDamageReceived = 0;
            boolean attackerDead = false;
            boolean attackerDealtFirstStrike = attacker.firstStrike || attacker.doubleStrike;

            // === First strike phase ===
            if (attackerDealtFirstStrike) {
                int remaining = attacker.power;
                for (CreatureInfo blocker : orderedBlockers) {
                    if (remaining <= 0) break;
                    if (!blocker.indestructible) {
                        int dmg = Math.min(remaining, blocker.toughness);
                        if (dmg >= blocker.toughness) {
                            defenderCreaturesLostValue += blocker.creatureScore;
                        }
                        remaining -= dmg;
                    } else {
                        remaining -= blocker.toughness;
                    }
                }
                if (attacker.trample && remaining > 0) {
                    if (attacker.infect) {
                        defenderPoisonGained += remaining;
                    } else {
                        defenderLifeLost += remaining;
                    }
                }
            }

            // Blockers with first strike deal damage to attacker
            for (CreatureInfo blocker : orderedBlockers) {
                if (blocker.firstStrike || blocker.doubleStrike) {
                    attackerDamageReceived += blocker.power;
                    if (blocker.lifelink) {
                        defenderLifeGained += blocker.power;
                    }
                }
            }

            if (attackerDamageReceived >= attacker.toughness && !attacker.indestructible) {
                attackerDead = true;
                attackerCreaturesLostValue += attacker.creatureScore;
            }

            // === Regular damage phase ===
            if (!attackerDead && !(attacker.firstStrike && !attacker.doubleStrike)) {
                int remaining = attacker.power;
                for (CreatureInfo blocker : orderedBlockers) {
                    if (remaining <= 0) break;
                    if (!blocker.indestructible) {
                        int dmg = Math.min(remaining, blocker.toughness);
                        // Only count blocker death here if attacker didn't have first strike
                        if (dmg >= blocker.toughness && !attackerDealtFirstStrike) {
                            defenderCreaturesLostValue += blocker.creatureScore;
                        }
                        remaining -= dmg;
                    } else {
                        remaining -= blocker.toughness;
                    }
                }
                if (attacker.trample && remaining > 0) {
                    if (attacker.infect) {
                        defenderPoisonGained += remaining;
                    } else {
                        defenderLifeLost += remaining;
                    }
                }
            }

            // Regular blocker damage to attacker
            if (!attackerDead) {
                int totalRegularBlockerDmg = 0;
                for (CreatureInfo blocker : orderedBlockers) {
                    if (!blocker.firstStrike || blocker.doubleStrike) {
                        totalRegularBlockerDmg += blocker.power;
                        if (blocker.lifelink && !(blocker.firstStrike || blocker.doubleStrike)) {
                            defenderLifeGained += blocker.power;
                        }
                    }
                }
                if (totalRegularBlockerDmg + attackerDamageReceived >= attacker.toughness
                        && !attacker.indestructible) {
                    attackerCreaturesLostValue += attacker.creatureScore;
                }
            }
        }

        // Scale life loss weight based on proximity to lethal — when damage
        // represents a large fraction of remaining life, prioritize blocking
        // over preserving creatures (enables correct chump-blocking decisions).
        double lifeWeight = 2.0;
        if (aiLife > 0) {
            double totalPotentialDamage = 0;
            for (CreatureInfo attacker : attackerInfos) {
                totalPotentialDamage += attacker.power;
            }
            double lethalRatio = totalPotentialDamage / aiLife;
            if (lethalRatio >= 0.5) {
                // At 50% of life: weight = 2.0, at 100%+ (lethal): weight = 5.0
                lifeWeight = 2.0 + 3.0 * Math.min(1.0, (lethalRatio - 0.5) / 0.5);
            }
        }

        double score = attackerCreaturesLostValue
                - defenderCreaturesLostValue
                - defenderLifeLost * lifeWeight
                - defenderPoisonGained * 4.0
                + defenderLifeGained * 0.5;

        // Heavy penalty when blocking still results in lethal damage
        if (defenderLifeLost >= aiLife || defenderPoisonGained + aiPoison >= 10) {
            score -= 1000;
        }

        return score;
    }

    private List<int[]> convertToAssignmentList(List<CreatureInfo> attackerInfos,
                                                 List<List<CreatureInfo>> blockAssignments) {
        List<int[]> result = new ArrayList<>();
        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            for (CreatureInfo blocker : blockAssignments.get(ai)) {
                result.add(new int[]{blocker.index, attackerInfos.get(ai).index});
            }
        }
        return result;
    }

    /**
     * CSP constraint check for the exhaustive search: every attacker's block count must
     * satisfy its legality rules. Currently checks menace (count ∈ {0} ∪ [2, ∞]).
     * Extend with CanBeBlockedByAtMostN / "can't be blocked by more than 1" if/when
     * the simulator starts tracking those restrictions.
     */
    private static boolean isValidBlockerAssignment(List<CreatureInfo> attackerInfos,
                                                    List<List<CreatureInfo>> assignments) {
        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            CreatureInfo attacker = attackerInfos.get(ai);
            int count = assignments.get(ai).size();
            if (attacker.menace && count == 1) return false;
        }
        return true;
    }

    private static boolean incrementMixedRadix(int[] choices, List<List<Integer>> legalTargets) {
        for (int i = choices.length - 1; i >= 0; i--) {
            choices[i]++;
            if (choices[i] <= legalTargets.get(i).size()) {
                return true;
            }
            choices[i] = 0;
        }
        return false;
    }

    private boolean hasLureEffect(GameData gameData, Permanent attacker) {
        boolean hasOnCard = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance);
        if (hasOnCard) return true;
        return gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
    }

    private boolean hasMustBeBlockedIfAbleEffect(GameData gameData, Permanent attacker) {
        if (attacker.isMustBeBlockedThisTurn()) return true;
        boolean hasOnCard = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance);
        if (hasOnCard) return true;
        return gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
    }

    /**
     * Simulates combat between a set of attackers and greedy-optimal blocking.
     */
    CombatOutcome simulateCombat(GameData gameData, List<CreatureInfo> attackers, List<CreatureInfo> blockers,
                                  int opponentLife) {
        // Simulate opponent's greedy-optimal blocking:
        // assign best blocker to biggest threat first
        List<CreatureInfo> sortedAttackers = new ArrayList<>(attackers);
        sortedAttackers.sort(Comparator.comparingDouble(CreatureInfo::creatureScore).reversed());

        boolean[] blockerUsed = new boolean[blockers.size()];
        // Track blocking assignments: for each attacker, list of assigned blockers
        List<List<CreatureInfo>> blockAssignments = new ArrayList<>();
        for (int i = 0; i < sortedAttackers.size(); i++) {
            blockAssignments.add(new ArrayList<>());
        }

        for (int i = 0; i < sortedAttackers.size(); i++) {
            CreatureInfo attacker = sortedAttackers.get(i);
            if (attacker.cantBeBlocked) continue;

            if (attacker.menace) {
                // Need 2 blockers for menace
                List<Integer> availableIdx = new ArrayList<>();
                for (int j = 0; j < blockers.size(); j++) {
                    if (!blockerUsed[j] && canBlock(gameData, blockers.get(j), attacker)) {
                        availableIdx.add(j);
                    }
                }
                if (availableIdx.size() >= 2) {
                    // Pick cheapest pair that can kill attacker
                    int bestA = -1, bestB = -1;
                    double bestValue = Double.NEGATIVE_INFINITY;
                    for (int a = 0; a < availableIdx.size(); a++) {
                        for (int b = a + 1; b < availableIdx.size(); b++) {
                            CreatureInfo ba = blockers.get(availableIdx.get(a));
                            CreatureInfo bb = blockers.get(availableIdx.get(b));
                            boolean kills = ba.power + bb.power >= attacker.toughness;
                            double value = kills ? attacker.creatureScore - ba.creatureScore - bb.creatureScore : -100;
                            if (value > bestValue) {
                                bestValue = value;
                                bestA = availableIdx.get(a);
                                bestB = availableIdx.get(b);
                            }
                        }
                    }
                    if (bestA >= 0 && bestValue > -50) {
                        blockerUsed[bestA] = true;
                        blockerUsed[bestB] = true;
                        blockAssignments.get(i).add(blockers.get(bestA));
                        blockAssignments.get(i).add(blockers.get(bestB));
                    }
                }
                continue;
            }

            // Single blocker: find most favorable block for defender
            int bestBlockerIdx = -1;
            double bestDefenderValue = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < blockers.size(); j++) {
                if (blockerUsed[j]) continue;
                CreatureInfo blocker = blockers.get(j);
                if (!canBlock(gameData, blocker, attacker)) continue;

                double value = evaluateDefenderBlock(attacker, blocker);
                if (value > bestDefenderValue) {
                    bestDefenderValue = value;
                    bestBlockerIdx = j;
                }
            }

            if (bestBlockerIdx >= 0 && bestDefenderValue > 0) {
                blockerUsed[bestBlockerIdx] = true;
                blockAssignments.get(i).add(blockers.get(bestBlockerIdx));
            }
        }

        // Now compute combat outcome
        int opponentLifeLost = 0;
        int opponentPoisonGained = 0;
        int aiLifeGained = 0;
        double aiCreaturesLostValue = 0;
        double oppCreaturesLostValue = 0;

        for (int i = 0; i < sortedAttackers.size(); i++) {
            CreatureInfo attacker = sortedAttackers.get(i);
            List<CreatureInfo> assignedBlockers = blockAssignments.get(i);

            if (assignedBlockers.isEmpty()) {
                // Unblocked: damage goes to opponent
                if (attacker.infect) {
                    opponentPoisonGained += attacker.power;
                } else {
                    opponentLifeLost += attacker.power;
                }
                if (attacker.lifelink) {
                    aiLifeGained += attacker.power;
                }
            } else {
                // Blocked combat
                int totalBlockerPower = assignedBlockers.stream().mapToInt(CreatureInfo::power).sum();
                int totalBlockerToughness = assignedBlockers.stream().mapToInt(CreatureInfo::toughness).sum();

                // First strike / Double strike phase
                int attackerDamageDealt = 0;
                int blockerDamageDealt = 0;

                if (attacker.firstStrike || attacker.doubleStrike) {
                    // Attacker deals first strike damage
                    int fsRemaining = attacker.power;
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.indestructible) {
                            int lethal = blocker.toughness;
                            int dmg = Math.min(fsRemaining, lethal);
                            if (dmg >= blocker.toughness) {
                                oppCreaturesLostValue += blocker.creatureScore;
                            }
                            fsRemaining -= dmg;
                        } else {
                            fsRemaining -= blocker.toughness;
                        }
                        if (fsRemaining <= 0) break;
                    }

                    // Trample excess in first strike
                    if (attacker.trample && fsRemaining > 0) {
                        if (attacker.infect) {
                            opponentPoisonGained += fsRemaining;
                        } else {
                            opponentLifeLost += fsRemaining;
                        }
                    }
                    if (attacker.lifelink) {
                        aiLifeGained += attacker.power;
                    }
                    attackerDamageDealt += attacker.power;
                }

                // Check which blockers have first strike
                for (CreatureInfo blocker : assignedBlockers) {
                    if (blocker.firstStrike || blocker.doubleStrike) {
                        blockerDamageDealt += blocker.power;
                    }
                }

                // Check if attacker dies to first strike damage
                boolean attackerDead = false;
                if (blockerDamageDealt >= attacker.toughness && !attacker.indestructible) {
                    attackerDead = true;
                    aiCreaturesLostValue += attacker.creatureScore;
                }

                // Regular damage phase (only if not already dealt via first strike)
                if (!attackerDead && !(attacker.firstStrike && !attacker.doubleStrike)) {
                    int regularRemaining = attacker.power;
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.indestructible) {
                            int dmg = Math.min(regularRemaining, blocker.toughness);
                            // Only count as killed if not already killed in first strike
                            if (dmg >= blocker.toughness && !attacker.firstStrike && !attacker.doubleStrike) {
                                oppCreaturesLostValue += blocker.creatureScore;
                            }
                            regularRemaining -= dmg;
                        } else {
                            regularRemaining -= blocker.toughness;
                        }
                        if (regularRemaining <= 0) break;
                    }

                    if (attacker.trample && regularRemaining > 0) {
                        if (attacker.infect) {
                            opponentPoisonGained += regularRemaining;
                        } else {
                            opponentLifeLost += regularRemaining;
                        }
                    }
                    if (attacker.lifelink && attackerDamageDealt == 0) {
                        aiLifeGained += attacker.power;
                    }
                }

                // Regular blocker damage
                if (!attackerDead) {
                    int totalRegularBlockerDmg = 0;
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.firstStrike || blocker.doubleStrike) {
                            totalRegularBlockerDmg += blocker.power;
                        }
                    }
                    if (totalRegularBlockerDmg + blockerDamageDealt >= attacker.toughness
                            && !attacker.indestructible) {
                        aiCreaturesLostValue += attacker.creatureScore;
                    }
                }
            }
        }

        return new CombatOutcome(aiLifeGained, -opponentLifeLost, opponentPoisonGained,
                aiCreaturesLostValue, oppCreaturesLostValue);
    }

    private double evaluateBlock(CreatureInfo attacker, CreatureInfo blocker) {
        boolean blockerKillsAttacker = blocker.power >= attacker.toughness || attacker.indestructible;
        boolean attackerKillsBlocker = attacker.power >= blocker.toughness && !blocker.indestructible;

        if (blocker.power >= attacker.toughness && !attacker.indestructible) {
            if (!attackerKillsBlocker) {
                // Kill attacker, blocker survives — great trade
                return attacker.creatureScore + 10;
            }
            // Both die — trade evaluation
            return attacker.creatureScore - blocker.creatureScore;
        }

        if (!attackerKillsBlocker) {
            // Neither dies — still prevents damage
            return attacker.power * 0.5;
        }

        // Blocker dies, attacker lives — bad unless chump blocking
        return -blocker.creatureScore + attacker.power * 0.5;
    }

    /**
     * Evaluates blocking a trample attacker. Unlike evaluateBlock, this factors in how much
     * trample damage the blocker's toughness prevents, preferring high-toughness blockers
     * even when they can't kill the attacker.
     */
    private double evaluateTrampleBlock(CreatureInfo attacker, CreatureInfo blocker) {
        boolean blockerKillsAttacker = blocker.power >= attacker.toughness && !attacker.indestructible;
        boolean attackerKillsBlocker = attacker.power >= blocker.toughness && !blocker.indestructible;

        if (blockerKillsAttacker) {
            if (!attackerKillsBlocker) {
                // Kill attacker and blocker survives — best outcome, no trample at all
                return attacker.creatureScore + 10;
            }
            // Both die — trade evaluation
            return attacker.creatureScore - blocker.creatureScore;
        }

        // Attacker survives with trample: value is based on how much trample damage is prevented.
        // The blocker's toughness is how much of the attacker's power it absorbs.
        int tramplePrevented = Math.min(attacker.power, blocker.toughness);
        double cost = attackerKillsBlocker ? blocker.creatureScore : 0;
        return tramplePrevented * 0.8 - cost;
    }

    private double evaluateDefenderBlock(CreatureInfo attacker, CreatureInfo blocker) {
        // From defender's perspective: positive means favorable for the defender
        boolean blockerKillsAttacker = blocker.power >= attacker.toughness && !attacker.indestructible;
        boolean attackerKillsBlocker = attacker.power >= blocker.toughness && !blocker.indestructible;

        if (blockerKillsAttacker && !attackerKillsBlocker) {
            // Defender kills attacker, blocker survives
            return attacker.creatureScore + 10;
        }
        if (blockerKillsAttacker && attackerKillsBlocker) {
            // Both die — favorable if attacker is more valuable
            return attacker.creatureScore - blocker.creatureScore;
        }
        if (!blockerKillsAttacker && !attackerKillsBlocker) {
            // Neither dies — prevents damage
            return attacker.power * 0.3;
        }
        // Blocker dies, attacker survives — only block if cheap blocker vs expensive attacker
        return -blocker.creatureScore * 0.5;
    }

    private int[] findBestBlockerPairForMenace(CreatureInfo attacker, List<CreatureInfo> available,
                                               boolean[] blockerUsed) {
        int[] bestPair = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (int a = 0; a < available.size(); a++) {
            for (int b = a + 1; b < available.size(); b++) {
                CreatureInfo ba = available.get(a);
                CreatureInfo bb = available.get(b);
                boolean kills = ba.power + bb.power >= attacker.toughness;
                boolean oneSurvives = attacker.power < ba.toughness || attacker.power < bb.toughness;

                double value;
                if (kills && oneSurvives) {
                    value = attacker.creatureScore;
                } else if (kills) {
                    value = attacker.creatureScore - ba.creatureScore - bb.creatureScore;
                } else {
                    continue;
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestPair = new int[]{ba.index, bb.index};
                }
            }
        }

        return bestValue > 0 ? bestPair : null;
    }

    private boolean canBlock(GameData gameData, CreatureInfo blocker, CreatureInfo attacker) {
        if (attacker.cantBeBlocked) return false;
        List<Permanent> defenderBattlefield = findBattlefieldFor(gameData, blocker.perm);
        return gameQueryService.canBlockAttacker(gameData, blocker.perm, attacker.perm, defenderBattlefield);
    }

    private List<Permanent> findBattlefieldFor(GameData gameData, Permanent perm) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf != null && bf.contains(perm)) {
                return bf;
            }
        }
        return List.of();
    }

    private boolean canBeBlockedByAny(GameData gameData, CreatureInfo attacker, List<CreatureInfo> blockers) {
        if (attacker.cantBeBlocked) return false;
        return blockers.stream().anyMatch(b -> canBlock(gameData, b, attacker));
    }

    CreatureInfo buildCreatureInfo(GameData gameData, Permanent perm, int index,
                                           UUID controllerId, UUID opponentId) {
        return buildCreatureInfo(gameData, perm, index, controllerId, opponentId, null);
    }

    CreatureInfo buildCreatureInfo(GameData gameData, Permanent perm, int index,
                                           UUID controllerId, UUID opponentId,
                                           List<Permanent> defenderBattlefield) {
        int power = gameQueryService.getEffectivePower(gameData, perm);
        int toughness = gameQueryService.getEffectiveToughness(gameData, perm);

        boolean cantBeBlocked = gameQueryService.hasCantBeBlocked(gameData, perm)
                || isCantBeBlockedDueToDefenderCondition(gameData, perm, defenderBattlefield)
                || isCantBeBlockedDueToHistoricCast(gameData, perm, controllerId)
                || hasLandwalkAgainstDefender(gameData, perm, defenderBattlefield);

        // Temporarily stolen creatures (e.g. via Act of Treason) have no permanent value to the
        // controller — they will be returned at end of turn regardless. Treat their combat loss
        // value as 0 so the AI doesn't incorrectly avoid attacking with them out of fear of
        // "losing" a creature that was never truly theirs to keep.
        boolean isStolenUntilEndOfTurn = gameData.untilEndOfTurnStolenCreatures.contains(perm.getId());
        double score = isStolenUntilEndOfTurn ? 0
                : boardEvaluator.creatureScore(gameData, perm, controllerId, opponentId);

        return new CreatureInfo(
                index,
                perm.getId(),
                perm,
                power,
                toughness,
                gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING),
                gameQueryService.hasKeyword(gameData, perm, Keyword.FIRST_STRIKE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.DOUBLE_STRIKE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.TRAMPLE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.LIFELINK),
                gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.MENACE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.FEAR),
                gameQueryService.hasKeyword(gameData, perm, Keyword.INTIMIDATE),
                gameQueryService.hasKeyword(gameData, perm, Keyword.REACH),
                gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER),
                cantBeBlocked,
                gameQueryService.isArtifact(perm),
                gameQueryService.hasKeyword(gameData, perm, Keyword.INFECT),
                perm.getCard().getColor(),
                score
        );
    }

    private boolean hasLandwalkAgainstDefender(GameData gameData, Permanent attacker,
                                                List<Permanent> defenderBattlefield) {
        if (defenderBattlefield == null) return false;
        for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
            if (gameQueryService.hasKeyword(gameData, attacker, entry.getKey())
                    && defenderBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isCantBeBlockedDueToHistoricCast(GameData gameData, Permanent attacker, UUID controllerId) {
        return attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect.class::isInstance)
                && controllerId != null
                && gameQueryService.playerCastHistoricSpellThisTurn(gameData, controllerId);
    }

    private boolean isCantBeBlockedDueToDefenderCondition(GameData gameData, Permanent attacker,
                                                           List<Permanent> defenderBattlefield) {
        if (defenderBattlefield == null) return false;
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeBlockedIfDefenderControlsMatchingPermanentEffect restriction) {
                boolean defenderMatches = defenderBattlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (defenderMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===== Counter-Attack Estimation =====

    /**
     * Estimates the outcome of a greedy counter-attack by the opponent given the
     * AI's remaining blockers. Unlike {@link #simulateCombat}, this method models
     * chump blocking: when the total incoming damage would be lethal, the AI
     * blocks even at a negative trade because not blocking would lose the game.
     * <p>
     * Returns a two-element array: {@code [damageTaken, creaturesLostValue]}.
     * Infect damage is counted as 2x its power in life-equivalent terms (since
     * 10 poison = loss, and 20 life = loss).
     */
    double[] estimateCounterAttackOutcome(GameData gameData,
                                           List<CreatureInfo> attackers,
                                           List<CreatureInfo> blockers,
                                           int aiLife) {
        if (attackers.isEmpty()) return new double[]{0, 0};

        // Total raw incoming damage if we don't block at all (used for lethal-chump check)
        int rawIncoming = 0;
        for (CreatureInfo a : attackers) {
            rawIncoming += a.power;
        }

        // Process attackers biggest-first so the AI uses blockers on the largest threats
        List<CreatureInfo> sorted = new ArrayList<>(attackers);
        sorted.sort(Comparator.comparingInt((CreatureInfo c) -> c.power).reversed());

        boolean[] blockerUsed = new boolean[blockers.size()];
        double damageTaken = 0;
        double creaturesLostValue = 0;
        int remainingIncoming = rawIncoming;

        for (CreatureInfo attacker : sorted) {
            int unblockedPower = attacker.power;

            if (attacker.cantBeBlocked) {
                damageTaken += unblockedPower;
                remainingIncoming -= unblockedPower;
                continue;
            }

            // Find the best single blocker for this attacker
            int bestIdx = -1;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < blockers.size(); j++) {
                if (blockerUsed[j]) continue;
                CreatureInfo b = blockers.get(j);
                if (!canBlock(gameData, b, attacker)) continue;
                double v = attacker.trample
                        ? evaluateTrampleBlock(attacker, b)
                        : evaluateBlock(attacker, b);
                if (v > bestValue) {
                    bestValue = v;
                    bestIdx = j;
                }
            }

            boolean mustChump = remainingIncoming >= aiLife;
            if (bestIdx >= 0 && (bestValue > 0 || mustChump)) {
                CreatureInfo b = blockers.get(bestIdx);
                blockerUsed[bestIdx] = true;

                boolean blockerDies = !b.indestructible && attacker.power >= b.toughness;
                if (blockerDies) {
                    creaturesLostValue += b.creatureScore;
                }

                if (attacker.trample) {
                    int trampleDmg = Math.max(0, attacker.power - b.toughness);
                    damageTaken += trampleDmg;
                    remainingIncoming -= (unblockedPower - trampleDmg);
                } else {
                    // All damage absorbed by the blocker
                    remainingIncoming -= unblockedPower;
                }
            } else {
                // Not blocked: full damage goes through
                damageTaken += unblockedPower;
                remainingIncoming -= unblockedPower;
            }
        }

        return new double[]{damageTaken, creaturesLostValue};
    }

    // ===== Defensive Value Penalty =====

    /**
     * Snapshot of the opponent's potential next-turn attack when every AI creature
     * is available as a blocker. Used as the baseline against which per-subset
     * reductions are compared: attacking creatures (without vigilance) get removed
     * from the blocker pool, and the incremental damage / creature loss is the
     * penalty for attacking with them.
     */
    record DefensiveBaseline(double damageTaken, double creaturesLostValue) {}

    /**
     * Collects the opponent's creatures that could legally attack on their next
     * turn — non-defender creatures with positive power. Currently-tapped
     * creatures are included because they will untap at the opponent's next
     * untap step; summoning sickness similarly resolves by then.
     */
    List<CreatureInfo> buildOpponentNextTurnAttackers(GameData gameData, UUID aiPlayerId,
                                                       UUID opponentId,
                                                       List<Permanent> oppBattlefield,
                                                       List<Permanent> aiBattlefield) {
        List<CreatureInfo> result = new ArrayList<>();
        if (opponentId == null) return result;
        for (int i = 0; i < oppBattlefield.size(); i++) {
            Permanent perm = oppBattlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            int power = gameQueryService.getEffectivePower(gameData, perm);
            if (power <= 0) continue;
            result.add(buildCreatureInfo(gameData, perm, i, opponentId, aiPlayerId, aiBattlefield));
        }
        return result;
    }

    /**
     * Collects AI creatures that will be available to block on the opponent's
     * next turn. Currently-tapped creatures are excluded because they will
     * still be tapped when the opponent attacks (AI's untap step hasn't come
     * around yet). Summoning-sick creatures are included — MTG rules allow
     * summoning-sick creatures to block.
     */
    List<CreatureInfo> buildAiPotentialBlockers(GameData gameData, UUID aiPlayerId,
                                                 UUID opponentId, List<Permanent> aiBattlefield) {
        List<CreatureInfo> result = new ArrayList<>();
        for (int i = 0; i < aiBattlefield.size(); i++) {
            Permanent perm = aiBattlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            result.add(buildCreatureInfo(gameData, perm, i, aiPlayerId, opponentId));
        }
        return result;
    }

    /**
     * Computes a penalty reflecting the defensive value we lose by attacking
     * with a given subset of creatures. Non-vigilance attackers become tapped
     * and cannot block the opponent's next-turn attack.
     * <p>
     * The penalty compares the opponent's counter-attack outcome with all AI
     * creatures as blockers (baseline) vs the subset of creatures remaining
     * after the attack. Extra life lost and extra creature value lost both
     * contribute to the penalty. If attacking converts a survivable
     * counter-attack into a lethal one, a large sentinel penalty is added.
     * <p>
     * The penalty weight scales with urgency: when the AI's post-counter life
     * total is low, each point of lost life hurts more.
     */
    double computeDefensiveValuePenalty(GameData gameData,
                                        List<CreatureInfo> attackSubset,
                                        List<CreatureInfo> aiPotentialBlockers,
                                        List<CreatureInfo> opponentNextTurnAttackers,
                                        DefensiveBaseline baseline,
                                        int aiLife) {
        if (baseline == null || opponentNextTurnAttackers.isEmpty()) return 0;
        if (attackSubset.isEmpty()) return 0;

        // Identify which AI creatures would be tapped (non-vigilance attackers).
        // Vigilance attackers stay untapped and can still block, so they don't
        // reduce our defensive capability.
        Set<UUID> tappedAttackerIds = new HashSet<>();
        for (CreatureInfo info : attackSubset) {
            if (gameQueryService.hasKeyword(gameData, info.perm, Keyword.VIGILANCE)) continue;
            tappedAttackerIds.add(info.id);
        }
        if (tappedAttackerIds.isEmpty()) return 0;

        // Remove tapped attackers from the potential blocker pool.
        List<CreatureInfo> remainingBlockers = new ArrayList<>(aiPotentialBlockers.size());
        for (CreatureInfo blocker : aiPotentialBlockers) {
            if (!tappedAttackerIds.contains(blocker.id)) {
                remainingBlockers.add(blocker);
            }
        }
        // If we didn't actually remove anything (e.g. attackers weren't in the
        // potential-blocker set for some reason), the attack doesn't reduce defense.
        if (remainingBlockers.size() == aiPotentialBlockers.size()) return 0;

        double[] reducedOutcome = estimateCounterAttackOutcome(
                gameData, opponentNextTurnAttackers, remainingBlockers, aiLife);
        double reducedDamage = reducedOutcome[0];
        double reducedCreatureLoss = reducedOutcome[1];

        double damageDelta = reducedDamage - baseline.damageTaken();
        double creatureLossDelta = reducedCreatureLoss - baseline.creaturesLostValue();

        // If the attack doesn't worsen our defensive outcome, there's nothing to penalize.
        if (damageDelta <= 0 && creatureLossDelta <= 0) return 0;

        double penalty = 0;

        // Lethal flip sentinel: attacking turns a survivable counter-attack into a
        // lethal one. This is the worst-case scenario and must strongly outweigh
        // any attack upside short of actual lethal this turn.
        double lifeAfterBaseline = aiLife - baseline.damageTaken();
        double lifeAfterReduced = aiLife - reducedDamage;
        if (lifeAfterBaseline > 0 && lifeAfterReduced <= 0) {
            penalty += 1000;
        }

        // Base penalty: each point of extra damage scales roughly like the
        // attacker-side life-loss weight (2.0 per point) used in evaluationDelta,
        // so the defensive trade-off is commensurate with offensive gain.
        penalty += Math.max(0, damageDelta) * 2.0;

        // Creature loss delta: extra blockers we'd lose in the counter-attack.
        penalty += Math.max(0, creatureLossDelta);

        // Urgency scaling: at full life the penalty stays baseline; as our
        // post-counter life falls, each point hurts more (up to ~2x at near-zero life).
        if (lifeAfterReduced > 0 && aiLife > 0) {
            double urgency = 1.0 + (1.0 - Math.min(1.0, lifeAfterReduced / (double) aiLife));
            penalty *= urgency;
        }

        return penalty;
    }

    // ===== Opponent Trick Risk Estimation =====

    /**
     * Computes a risk penalty for a set of attackers when the opponent may have
     * combat tricks. For each blockable attacker, checks whether any legal blocker
     * — when pumped by the estimated trick — could kill the attacker when it
     * wouldn't die without the trick. This captures the key scenario: the opponent
     * blocks only <em>because</em> they have a trick, turning a safe attack into a
     * creature loss.
     * <p>
     * Returns the maximum such vulnerability (the single best target for the
     * opponent's trick) scaled by trick probability.
     */
    double computeAttackTrickRisk(GameData gameData,
                                  List<CreatureInfo> attackers,
                                  List<CreatureInfo> blockers,
                                  OpponentThreatEstimator.ThreatEstimate threat) {
        if (!threat.hasThreat()) return 0;
        int pump = threat.estimatedPumpBoost();

        double maxVulnerability = 0;

        for (CreatureInfo attacker : attackers) {
            if (attacker.cantBeBlocked || attacker.indestructible) continue;

            for (CreatureInfo blocker : blockers) {
                if (!canBlock(gameData, blocker, attacker)) continue;

                boolean alreadyLethal = blocker.power >= attacker.toughness;
                boolean pumpedLethal = (blocker.power + pump) >= attacker.toughness;

                if (pumpedLethal && !alreadyLethal) {
                    // The pump turns a non-lethal block into a lethal one for our attacker.
                    // This is the creature value at risk if the opponent has a trick.
                    maxVulnerability = Math.max(maxVulnerability, attacker.creatureScore);
                    break; // one vulnerable blocker is enough for this attacker
                }
            }
        }

        return maxVulnerability * threat.trickProbability();
    }

    /**
     * Computes a risk penalty for a set of blocker assignments when the opponent may
     * have combat tricks. For each blocked attacker, evaluates how the defender's combat
     * score changes if that attacker receives the estimated pump. The penalty is the
     * worst-case swing (the single best pump target for the opponent) scaled by trick
     * probability. This captures the key scenario: a profitable-looking block that flips
     * to a disaster when the opponent casts a pump spell on their creature.
     * <p>
     * Only the worst single attacker is considered because a typical opponent has at
     * most one pump spell in hand — they can't pump every attacker simultaneously.
     */
    double computeBlockTrickRisk(List<CreatureInfo> attackerInfos,
                                  List<List<CreatureInfo>> blockAssignments,
                                  int aiLife, int aiPoison,
                                  OpponentThreatEstimator.ThreatEstimate threat) {
        if (!threat.hasThreat()) return 0;
        int pump = threat.estimatedPumpBoost();

        double baselineScore = evaluateDefenderCombat(attackerInfos, blockAssignments, aiLife, aiPoison);
        double maxSwing = 0;

        for (int ai = 0; ai < attackerInfos.size(); ai++) {
            // Unblocked attackers don't interact with our creatures — a pump on them
            // would deal a bit more damage, but that applies equally whether we block
            // elsewhere or not, so it doesn't affect the relative ranking of blocks.
            if (blockAssignments.get(ai).isEmpty()) continue;

            CreatureInfo original = attackerInfos.get(ai);
            if (original.indestructible) {
                // Pump toughness doesn't matter for an already-indestructible attacker,
                // but pumped power could still kill blockers. Continue to simulate.
            }

            CreatureInfo pumped = withPump(original, pump);
            List<CreatureInfo> pumpedList = new ArrayList<>(attackerInfos);
            pumpedList.set(ai, pumped);

            double pumpedScore = evaluateDefenderCombat(pumpedList, blockAssignments, aiLife, aiPoison);
            double swing = baselineScore - pumpedScore;
            if (swing > maxSwing) {
                maxSwing = swing;
            }
        }

        return maxSwing * threat.trickProbability();
    }

    /**
     * Returns a copy of the given CreatureInfo with power and toughness increased by
     * the given pump amount. Used by combat trick risk estimation.
     */
    private static CreatureInfo withPump(CreatureInfo c, int pump) {
        return new CreatureInfo(
                c.index, c.id, c.perm,
                c.power + pump, c.toughness + pump,
                c.flying, c.firstStrike, c.doubleStrike, c.trample, c.lifelink,
                c.indestructible, c.menace, c.fear, c.intimidate, c.reach, c.defender,
                c.cantBeBlocked, c.isArtifact, c.infect, c.color, c.creatureScore);
    }

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
