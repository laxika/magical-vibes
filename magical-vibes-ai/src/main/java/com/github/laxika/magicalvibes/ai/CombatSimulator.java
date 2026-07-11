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
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Exhaustive combat search with pure arithmetic (no state mutation).
 * Simulates combat outcomes to find the best set of attackers or blockers.
 *
 * <p>This class owns the searches (which subsets of attackers/blockers to consider) and the
 * GameData-backed setup: building {@link CreatureInfo} snapshots and precomputing block
 * legality into {@link CombatMath.Attacker} rows. The per-configuration combat arithmetic
 * itself lives in {@link CombatMath}, which never touches GameData — the attacker subset
 * loop evaluates up to 2^12 configurations per decision, so everything inside it must stay
 * free of layered-system queries.
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final BoardEvaluator boardEvaluator;

    public CombatSimulator(GameQueryService gameQueryService, BoardEvaluator boardEvaluator) {
        this.predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
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
        List<CreatureInfo> aiPotentialBlockers = List.of();
        List<CombatMath.Attacker> opponentNextTurnAttackers = List.of();
        DefensiveBaseline defensiveBaseline = null;
        if (aiLife > 0 && aiLife < DEFENSIVE_PENALTY_LIFE_THRESHOLD) {
            List<CreatureInfo> opponentAttackerInfos = buildOpponentNextTurnAttackers(
                    gameData, aiPlayerId, opponentId, oppBattlefield, aiBattlefield);
            if (!opponentAttackerInfos.isEmpty()) {
                aiPotentialBlockers = buildAiPotentialBlockers(
                        gameData, aiPlayerId, opponentId, aiBattlefield);
                opponentNextTurnAttackers = buildAttackers(
                        gameData, opponentAttackerInfos, aiPotentialBlockers, false);
                double[] baselineOutcome = CombatMath.estimateCounterAttackOutcome(
                        opponentNextTurnAttackers, aiPotentialBlockers, null, aiLife);
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
            return indexList(forcedAttackerInfos);
        }

        // Precompute each candidate attacker's block-compatibility row once. The subset loop
        // below evaluates combat up to 2^12 times, and block legality is by far the most
        // expensive per-pair query involved (layered-system keyword checks).
        boolean trackVigilance = defensiveBaseline != null;
        List<CombatMath.Attacker> forcedAttackers = buildAttackers(
                gameData, forcedAttackerInfos, blockerInfos, trackVigilance);
        List<CombatMath.Attacker> optionalAttackers = buildAttackers(
                gameData, optionalAttackerInfos, blockerInfos, trackVigilance);

        int n = optionalAttackers.size();
        int totalSubsets = 1 << n;

        // Baseline: forced attackers only (score must beat this; if no must-attack, baseline is 0 = no attack)
        double bestScore;
        List<Integer> bestSubset = List.of();
        if (!forcedAttackers.isEmpty()) {
            // Evaluate forced-only attack as baseline
            CombatOutcome forcedOutcome = CombatMath.simulateCombat(forcedAttackers, blockerInfos, opponentLife);
            bestScore = forcedOutcome.evaluationDelta();
            bestSubset = indexList(forcedAttackerInfos);
        } else {
            bestScore = 0; // Not attacking at all scores 0
        }

        List<CombatMath.Attacker> subset = new ArrayList<>(forcedAttackers.size() + n);
        for (int mask = 0; mask < totalSubsets; mask++) {
            // Build subset: forced attackers + selected optional attackers
            subset.clear();
            subset.addAll(forcedAttackers);
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(optionalAttackers.get(i));
                }
            }

            // Skip empty subset (no optional + no forced)
            if (subset.isEmpty()) continue;

            // Quick lethal check: if unblockable damage >= opponent life (or poison), pick immediately
            int unblockableLifeDamage = 0;
            int unblockablePoisonDamage = 0;
            for (CombatMath.Attacker attacker : subset) {
                if (attacker.blockable()) continue;
                if (attacker.info().infect()) {
                    unblockablePoisonDamage += attacker.info().power();
                } else {
                    unblockableLifeDamage += attacker.info().power();
                }
            }
            if (unblockableLifeDamage >= opponentLife
                    || unblockablePoisonDamage + opponentPoison >= 10) {
                return attackerIndexList(subset);
            }

            // Simulate greedy-optimal blocking by opponent
            CombatOutcome outcome = CombatMath.simulateCombat(subset, blockerInfos, opponentLife);
            double score = outcome.evaluationDelta();

            // Apply pessimism for opponent's potential combat tricks
            if (threatEstimate.hasThreat()) {
                score -= CombatMath.computeAttackTrickRisk(subset, blockerInfos, threatEstimate);
            }

            // Apply defensive value penalty: attacking taps non-vigilance creatures,
            // leaving them unable to block the opponent's counter-attack. When the
            // opponent has a significant board, sending creatures into a losing race
            // is worse than holding them back.
            if (defensiveBaseline != null) {
                Set<UUID> tappedAttackerIds = new HashSet<>();
                for (CombatMath.Attacker attacker : subset) {
                    if (!attacker.vigilance()) {
                        tappedAttackerIds.add(attacker.info().id());
                    }
                }
                score -= CombatMath.computeDefensiveValuePenalty(tappedAttackerIds, aiPotentialBlockers,
                        opponentNextTurnAttackers, defensiveBaseline, aiLife);
            }

            if (score > bestScore) {
                bestScore = score;
                bestSubset = attackerIndexList(subset);
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

        // Precompute the lure / must-block flags once — they involve effect scans and aura
        // queries, and the sort below would otherwise re-evaluate them per comparison.
        Set<UUID> lureAttackerIds = new HashSet<>();
        Set<UUID> mustBlockAttackerIds = new HashSet<>();
        for (CreatureInfo attacker : attackerInfos) {
            if (hasLureEffect(gameData, attacker.perm)) lureAttackerIds.add(attacker.id);
            if (hasMustBeBlockedIfAbleEffect(gameData, attacker.perm)) mustBlockAttackerIds.add(attacker.id);
        }

        // Priority: lure first (forces all able), then mustBlockIfAble, then regular by threat desc.
        // Within lures, menace+lure sorts ahead so it claims a legal 2+ pool before a non-menace
        // lure can drain candidates.
        List<CreatureInfo> sortedAttackers = new ArrayList<>(attackerInfos);
        sortedAttackers.sort((a, b) -> {
            int lureCmp = Boolean.compare(lureAttackerIds.contains(b.id), lureAttackerIds.contains(a.id));
            if (lureCmp != 0) return lureCmp;
            int menaceCmp = Boolean.compare(b.menace, a.menace);
            if (menaceCmp != 0) return menaceCmp;
            int mbCmp = Boolean.compare(mustBlockAttackerIds.contains(b.id),
                                        mustBlockAttackerIds.contains(a.id));
            if (mbCmp != 0) return mbCmp;
            return Double.compare(b.creatureScore, a.creatureScore);
        });

        int totalIncoming = 0;
        for (CreatureInfo attacker : attackerInfos) {
            totalIncoming += attacker.power;
        }
        boolean[] blockerUsed = new boolean[aiBattlefield.size()];
        List<int[]> assignments = new ArrayList<>();

        for (CreatureInfo attacker : sortedAttackers) {
            if (attacker.cantBeBlocked) continue;

            List<CreatureInfo> candidates = new ArrayList<>();
            for (CreatureInfo blocker : blockerInfos) {
                if (blockerUsed[blocker.index]) continue;
                if (!canBlock(gameData, blocker, attacker)) continue;
                candidates.add(blocker);
            }
            if (candidates.isEmpty()) continue;

            // Menace: no creature is "able to block" alone, so with <2 candidates skip entirely.
            if (attacker.menace && candidates.size() < 2) continue;

            boolean lure = lureAttackerIds.contains(attacker.id);
            boolean mustBlock = mustBlockAttackerIds.contains(attacker.id);
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
                        ? (attacker.trample ? CombatMath.evaluateTrampleBlock(attacker, best)
                                            : CombatMath.evaluateBlock(attacker, best))
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
        int[] bestPair = CombatMath.findBestBlockerPairForMenace(attacker, candidates);
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
                    ? CombatMath.evaluateTrampleBlock(attacker, blocker)
                    : CombatMath.evaluateBlock(attacker, blocker);
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
            scored.sort(Comparator.comparingDouble((CreatureInfo b) -> CombatMath.evaluateBlock(mustBlockAttacker, b)).reversed());
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
                double score = CombatMath.evaluateDefenderCombat(attackerInfos, forcedAssignments, aiLife, aiPoison);

                // Apply pessimism for opponent's potential combat tricks: a block that
                // looks profitable now could flip to a disaster if the opponent pumps an
                // attacker (e.g. 3/3 blocking 2/3 becomes a blocker loss after Giant Growth).
                if (threatEstimate.hasThreat()) {
                    score -= CombatMath.computeBlockTrickRisk(attackerInfos, forcedAssignments, aiLife, aiPoison, threatEstimate);
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
     * Higher score = better for the defender. Delegates to {@link CombatMath}.
     */
    double evaluateDefenderCombat(List<CreatureInfo> attackerInfos,
                                  List<List<CreatureInfo>> blockAssignments,
                                  int aiLife, int aiPoison) {
        return CombatMath.evaluateDefenderCombat(attackerInfos, blockAssignments, aiLife, aiPoison);
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
     * Convenience wrapper that computes block legality on the fly; the subset search in
     * {@link #findBestAttackers} precomputes the rows once and calls {@link CombatMath}
     * directly instead.
     */
    CombatOutcome simulateCombat(GameData gameData, List<CreatureInfo> attackers, List<CreatureInfo> blockers,
                                  int opponentLife) {
        return CombatMath.simulateCombat(buildAttackers(gameData, attackers, blockers, false),
                blockers, opponentLife);
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

    /**
     * Wraps attacker infos with their block-compatibility rows against the given blocker
     * list, evaluating the expensive per-pair block-legality query exactly once per pair.
     * When {@code trackVigilance} is set, each attacker's vigilance keyword is also
     * resolved (needed only by the defensive-value penalty).
     */
    private List<CombatMath.Attacker> buildAttackers(GameData gameData, List<CreatureInfo> attackers,
                                                     List<CreatureInfo> blockers, boolean trackVigilance) {
        List<CombatMath.Attacker> result = new ArrayList<>(attackers.size());
        for (CreatureInfo info : attackers) {
            boolean[] canBeBlockedBy = new boolean[blockers.size()];
            for (int j = 0; j < blockers.size(); j++) {
                canBeBlockedBy[j] = canBlock(gameData, blockers.get(j), info);
            }
            boolean vigilance = trackVigilance
                    && gameQueryService.hasKeyword(gameData, info.perm, Keyword.VIGILANCE);
            result.add(CombatMath.Attacker.of(info, canBeBlockedBy, vigilance));
        }
        return result;
    }

    private static List<Integer> indexList(List<CreatureInfo> infos) {
        List<Integer> result = new ArrayList<>(infos.size());
        for (CreatureInfo info : infos) {
            result.add(info.index);
        }
        return result;
    }

    private static List<Integer> attackerIndexList(List<CombatMath.Attacker> attackers) {
        List<Integer> result = new ArrayList<>(attackers.size());
        for (CombatMath.Attacker attacker : attackers) {
            result.add(attacker.info().index());
        }
        return result;
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
        boolean isStolenUntilEndOfTurn = gameData.isStolenUntilEndOfTurn(perm.getId());
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
                        .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
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
     * AI's remaining blockers. Convenience wrapper over
     * {@link CombatMath#estimateCounterAttackOutcome}; see that method for the model.
     * <p>
     * Returns a two-element array: {@code [damageTaken, creaturesLostValue]}.
     */
    double[] estimateCounterAttackOutcome(GameData gameData,
                                           List<CreatureInfo> attackers,
                                           List<CreatureInfo> blockers,
                                           int aiLife) {
        return CombatMath.estimateCounterAttackOutcome(
                buildAttackers(gameData, attackers, blockers, false), blockers, null, aiLife);
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
     * Computes the defensive value penalty for attacking with the given subset.
     * Convenience wrapper over {@link CombatMath#computeDefensiveValuePenalty} that
     * resolves vigilance and block legality on the fly; the subset search in
     * {@link #findBestAttackers} precomputes both and calls {@link CombatMath} directly.
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

        return CombatMath.computeDefensiveValuePenalty(tappedAttackerIds, aiPotentialBlockers,
                buildAttackers(gameData, opponentNextTurnAttackers, aiPotentialBlockers, false),
                baseline, aiLife);
    }

    // ===== Opponent Trick Risk Estimation =====

    /**
     * Computes a risk penalty for a set of attackers when the opponent may have
     * combat tricks. Convenience wrapper over {@link CombatMath#computeAttackTrickRisk};
     * see that method for the model.
     */
    double computeAttackTrickRisk(GameData gameData,
                                  List<CreatureInfo> attackers,
                                  List<CreatureInfo> blockers,
                                  OpponentThreatEstimator.ThreatEstimate threat) {
        return CombatMath.computeAttackTrickRisk(
                buildAttackers(gameData, attackers, blockers, false), blockers, threat);
    }

    /**
     * Computes a risk penalty for a set of blocker assignments when the opponent may
     * have combat tricks. Delegates to {@link CombatMath#computeBlockTrickRisk}.
     */
    double computeBlockTrickRisk(List<CreatureInfo> attackerInfos,
                                  List<List<CreatureInfo>> blockAssignments,
                                  int aiLife, int aiPoison,
                                  OpponentThreatEstimator.ThreatEstimate threat) {
        return CombatMath.computeBlockTrickRisk(attackerInfos, blockAssignments, aiLife, aiPoison, threat);
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
