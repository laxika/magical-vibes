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
        if (availableAttackerIndices.isEmpty()) {
            return List.of();
        }

        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        int opponentLife = gameData.getLife(opponentId);
        int opponentPoison = gameData.playerPoisonCounters.getOrDefault(opponentId, 0);

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

            if (score > bestScore) {
                bestScore = score;
                bestSubset = subset.stream().map(CreatureInfo::index).toList();
            }
        }

        return bestSubset;
    }

    /**
     * Finds the best blocker assignments for the AI as defender.
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

        // Build attacker and blocker info
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

        // Sort attackers by threat (highest power first)
        attackerInfos.sort(Comparator.comparingDouble(CreatureInfo::creatureScore).reversed());

        // Calculate total incoming damage to determine if lethal
        int totalIncoming = attackerInfos.stream().mapToInt(CreatureInfo::power).sum();
        boolean lethalIncoming = totalIncoming >= aiLife;

        List<int[]> assignments = new ArrayList<>(); // [blockerIdx, attackerIdx]
        boolean[] blockerUsed = new boolean[aiBattlefield.size()];

        // Phase 1: Enforce "must be blocked by all creatures" (Lure / Prized Unicorn):
        // any blocker that can block a lure attacker MUST do so — assign these first.
        List<CreatureInfo> lureAttackers = attackerInfos.stream()
                .filter(a -> hasLureEffect(gameData, a.perm))
                .toList();
        if (!lureAttackers.isEmpty()) {
            Set<Integer> lureAttackerIndicesBlocked = new HashSet<>();
            for (CreatureInfo blocker : blockerInfos) {
                for (CreatureInfo lureAttacker : lureAttackers) {
                    if (blockerUsed[blocker.index]) break;
                    if (!canBlock(gameData, blocker, lureAttacker)) continue;
                    assignments.add(new int[]{blocker.index, lureAttacker.index});
                    blockerUsed[blocker.index] = true;
                    lureAttackerIndicesBlocked.add(lureAttacker.index);
                }
            }
            // Update incoming damage estimate: blocked lure attackers won't deal face damage
            for (CreatureInfo lureAttacker : lureAttackers) {
                if (lureAttackerIndicesBlocked.contains(lureAttacker.index)) {
                    totalIncoming -= lureAttacker.power;
                }
            }
            lethalIncoming = totalIncoming >= aiLife;
        }

        // Phase 1b: Enforce "must be blocked if able" (Gaea's Protector style):
        // at least one blocker must block each such attacker if able.
        List<CreatureInfo> mustBeBlockedAttackers = attackerInfos.stream()
                .filter(a -> hasMustBeBlockedIfAbleEffect(gameData, a.perm))
                .toList();
        for (CreatureInfo mustBlockAttacker : mustBeBlockedAttackers) {
            boolean alreadyBlocked = assignments.stream()
                    .anyMatch(a -> a[1] == mustBlockAttacker.index);
            if (alreadyBlocked) continue;

            CreatureInfo bestBlocker = null;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (CreatureInfo blocker : blockerInfos) {
                if (blockerUsed[blocker.index]) continue;
                if (!canBlock(gameData, blocker, mustBlockAttacker)) continue;
                double value = evaluateBlock(mustBlockAttacker, blocker);
                if (value > bestValue) {
                    bestValue = value;
                    bestBlocker = blocker;
                }
            }
            if (bestBlocker != null) {
                assignments.add(new int[]{bestBlocker.index, mustBlockAttacker.index});
                blockerUsed[bestBlocker.index] = true;
                totalIncoming -= mustBlockAttacker.power;
                lethalIncoming = totalIncoming >= aiLife;
            }
        }

        // Phase 2: Evaluate regular blocks for remaining unused blockers
        for (CreatureInfo attacker : attackerInfos) {
            if (attacker.cantBeBlocked) continue;

            List<CreatureInfo> available = blockerInfos.stream()
                    .filter(b -> !blockerUsed[b.index])
                    .filter(b -> canBlock(gameData, b, attacker))
                    .toList();

            if (available.isEmpty()) continue;

            // Handle menace: need 2 blockers
            if (attacker.menace) {
                int[] bestPair = findBestBlockerPairForMenace(attacker, available, blockerUsed);
                if (bestPair != null) {
                    assignments.add(new int[]{bestPair[0], attacker.index});
                    assignments.add(new int[]{bestPair[1], attacker.index});
                    blockerUsed[bestPair[0]] = true;
                    blockerUsed[bestPair[1]] = true;
                    totalIncoming -= attacker.power;
                    lethalIncoming = totalIncoming >= aiLife;
                } else if (lethalIncoming && available.size() >= 2) {
                    // Chump block with cheapest pair
                    List<CreatureInfo> sorted = available.stream()
                            .sorted(Comparator.comparingDouble(CreatureInfo::creatureScore))
                            .toList();
                    if (sorted.size() >= 2) {
                        assignments.add(new int[]{sorted.get(0).index, attacker.index});
                        assignments.add(new int[]{sorted.get(1).index, attacker.index});
                        blockerUsed[sorted.get(0).index] = true;
                        blockerUsed[sorted.get(1).index] = true;
                        totalIncoming -= attacker.power;
                        lethalIncoming = totalIncoming >= aiLife;
                    }
                }
                continue;
            }

            // Find best single blocker
            CreatureInfo bestBlocker = null;
            double bestBlockValue = Double.NEGATIVE_INFINITY;

            for (CreatureInfo blocker : available) {
                double blockValue = evaluateBlock(attacker, blocker);
                if (blockValue > bestBlockValue) {
                    bestBlockValue = blockValue;
                    bestBlocker = blocker;
                }
            }

            // Only block if favorable or if lethal incoming
            if (bestBlocker != null && (bestBlockValue > 0 || lethalIncoming)) {
                assignments.add(new int[]{bestBlocker.index, attacker.index});
                blockerUsed[bestBlocker.index] = true;
                totalIncoming -= attacker.power;
                lethalIncoming = totalIncoming >= aiLife;
            }
        }

        return assignments;
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

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
