package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.CombatSimulator.CombatOutcome;
import com.github.laxika.magicalvibes.ai.CombatSimulator.CreatureInfo;
import com.github.laxika.magicalvibes.ai.CombatSimulator.DefensiveBaseline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Pure-arithmetic combat resolution for the AI's combat searches. Every method operates on
 * prebuilt {@link CreatureInfo} snapshots and precomputed block-compatibility rows — no
 * GameData or rules-engine queries — so {@link CombatSimulator#findBestAttackers} can call
 * them thousands of times per decision (once per attacker subset) without re-running the
 * expensive layered-system block-legality checks.
 */
final class CombatMath {

    private CombatMath() {
    }

    /**
     * An attacking creature plus its precomputed block compatibility: {@code canBeBlockedBy[j]}
     * is true when blocker {@code j} of the accompanying blocker list can legally block this
     * creature. Block legality ({@code GameQueryService.canBlockAttacker}) is an expensive
     * layered-system query, so {@link CombatSimulator} evaluates it once per attacker/blocker
     * pair and the row is reused across every subset evaluation. {@code blockable} caches
     * "any row entry is true"; {@code vigilance} is populated only when the defensive-value
     * penalty is active for the search.
     */
    record Attacker(CreatureInfo info, boolean[] canBeBlockedBy, boolean blockable, boolean vigilance) {

        static Attacker of(CreatureInfo info, boolean[] canBeBlockedBy, boolean vigilance) {
            boolean any = false;
            for (boolean b : canBeBlockedBy) {
                if (b) {
                    any = true;
                    break;
                }
            }
            return new Attacker(info, canBeBlockedBy, any, vigilance);
        }
    }

    /**
     * Simulates combat between a set of attackers and greedy-optimal blocking.
     */
    static CombatOutcome simulateCombat(List<Attacker> attackers, List<CreatureInfo> blockers,
                                        int opponentLife) {
        // Simulate opponent's greedy-optimal blocking:
        // assign best blocker to biggest threat first
        List<Attacker> sortedAttackers = new ArrayList<>(attackers);
        sortedAttackers.sort(Comparator.comparingDouble((Attacker a) -> a.info().creatureScore()).reversed());

        boolean[] blockerUsed = new boolean[blockers.size()];
        // Track blocking assignments: for each attacker, list of assigned blockers
        List<List<CreatureInfo>> blockAssignments = new ArrayList<>();
        for (int i = 0; i < sortedAttackers.size(); i++) {
            blockAssignments.add(new ArrayList<>());
        }

        for (int i = 0; i < sortedAttackers.size(); i++) {
            Attacker attacker = sortedAttackers.get(i);
            if (!attacker.blockable()) continue;
            CreatureInfo info = attacker.info();
            boolean[] canBeBlockedBy = attacker.canBeBlockedBy();

            if (info.menace()) {
                // Need 2 blockers for menace
                List<Integer> availableIdx = new ArrayList<>();
                for (int j = 0; j < blockers.size(); j++) {
                    if (!blockerUsed[j] && canBeBlockedBy[j]) {
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
                            boolean kills = ba.power() + bb.power() >= info.toughness();
                            double value = kills ? info.creatureScore() - ba.creatureScore() - bb.creatureScore() : -100;
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
                if (!canBeBlockedBy[j]) continue;

                double value = evaluateDefenderBlock(info, blockers.get(j));
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
            CreatureInfo attacker = sortedAttackers.get(i).info();
            List<CreatureInfo> assignedBlockers = blockAssignments.get(i);

            if (assignedBlockers.isEmpty()) {
                // Unblocked: damage goes to opponent
                if (attacker.infect()) {
                    opponentPoisonGained += attacker.power();
                } else {
                    opponentLifeLost += attacker.power();
                }
                if (attacker.lifelink()) {
                    aiLifeGained += attacker.power();
                }
            } else {
                // Blocked combat

                // First strike / Double strike phase
                int attackerDamageDealt = 0;
                int blockerDamageDealt = 0;

                if (attacker.firstStrike() || attacker.doubleStrike()) {
                    // Attacker deals first strike damage
                    int fsRemaining = attacker.power();
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.indestructible()) {
                            int lethal = blocker.toughness();
                            int dmg = Math.min(fsRemaining, lethal);
                            if (dmg >= blocker.toughness()) {
                                oppCreaturesLostValue += blocker.creatureScore();
                            }
                            fsRemaining -= dmg;
                        } else {
                            fsRemaining -= blocker.toughness();
                        }
                        if (fsRemaining <= 0) break;
                    }

                    // Trample excess in first strike
                    if (attacker.trample() && fsRemaining > 0) {
                        if (attacker.infect()) {
                            opponentPoisonGained += fsRemaining;
                        } else {
                            opponentLifeLost += fsRemaining;
                        }
                    }
                    if (attacker.lifelink()) {
                        aiLifeGained += attacker.power();
                    }
                    attackerDamageDealt += attacker.power();
                }

                // Check which blockers have first strike
                for (CreatureInfo blocker : assignedBlockers) {
                    if (blocker.firstStrike() || blocker.doubleStrike()) {
                        blockerDamageDealt += blocker.power();
                    }
                }

                // Check if attacker dies to first strike damage
                boolean attackerDead = false;
                if (blockerDamageDealt >= attacker.toughness() && !attacker.indestructible()) {
                    attackerDead = true;
                    aiCreaturesLostValue += attacker.creatureScore();
                }

                // Regular damage phase (only if not already dealt via first strike)
                if (!attackerDead && !(attacker.firstStrike() && !attacker.doubleStrike())) {
                    int regularRemaining = attacker.power();
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.indestructible()) {
                            int dmg = Math.min(regularRemaining, blocker.toughness());
                            // Only count as killed if not already killed in first strike
                            if (dmg >= blocker.toughness() && !attacker.firstStrike() && !attacker.doubleStrike()) {
                                oppCreaturesLostValue += blocker.creatureScore();
                            }
                            regularRemaining -= dmg;
                        } else {
                            regularRemaining -= blocker.toughness();
                        }
                        if (regularRemaining <= 0) break;
                    }

                    if (attacker.trample() && regularRemaining > 0) {
                        if (attacker.infect()) {
                            opponentPoisonGained += regularRemaining;
                        } else {
                            opponentLifeLost += regularRemaining;
                        }
                    }
                    if (attacker.lifelink() && attackerDamageDealt == 0) {
                        aiLifeGained += attacker.power();
                    }
                }

                // Regular blocker damage
                if (!attackerDead) {
                    int totalRegularBlockerDmg = 0;
                    for (CreatureInfo blocker : assignedBlockers) {
                        if (!blocker.firstStrike() || blocker.doubleStrike()) {
                            totalRegularBlockerDmg += blocker.power();
                        }
                    }
                    if (totalRegularBlockerDmg + blockerDamageDealt >= attacker.toughness()
                            && !attacker.indestructible()) {
                        aiCreaturesLostValue += attacker.creatureScore();
                    }
                }
            }
        }

        return new CombatOutcome(aiLifeGained, -opponentLifeLost, opponentPoisonGained,
                aiCreaturesLostValue, oppCreaturesLostValue);
    }

    /**
     * Evaluates a specific set of blocker assignments from the defender's perspective.
     * Higher score = better for the defender.
     */
    static double evaluateDefenderCombat(List<CreatureInfo> attackerInfos,
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
            if (attacker.menace() && blockers.size() < 2) {
                blockers = List.of();
            }

            if (blockers.isEmpty()) {
                if (attacker.infect()) {
                    defenderPoisonGained += attacker.power();
                } else {
                    defenderLifeLost += attacker.power();
                }
                continue;
            }

            // Defender controls damage assignment order — sacrifice cheapest first
            List<CreatureInfo> orderedBlockers = new ArrayList<>(blockers);
            orderedBlockers.sort(Comparator.comparingDouble(CreatureInfo::creatureScore));

            int attackerDamageReceived = 0;
            boolean attackerDead = false;
            boolean attackerDealtFirstStrike = attacker.firstStrike() || attacker.doubleStrike();

            // === First strike phase ===
            if (attackerDealtFirstStrike) {
                int remaining = attacker.power();
                for (CreatureInfo blocker : orderedBlockers) {
                    if (remaining <= 0) break;
                    if (!blocker.indestructible()) {
                        int dmg = Math.min(remaining, blocker.toughness());
                        if (dmg >= blocker.toughness()) {
                            defenderCreaturesLostValue += blocker.creatureScore();
                        }
                        remaining -= dmg;
                    } else {
                        remaining -= blocker.toughness();
                    }
                }
                if (attacker.trample() && remaining > 0) {
                    if (attacker.infect()) {
                        defenderPoisonGained += remaining;
                    } else {
                        defenderLifeLost += remaining;
                    }
                }
            }

            // Blockers with first strike deal damage to attacker
            for (CreatureInfo blocker : orderedBlockers) {
                if (blocker.firstStrike() || blocker.doubleStrike()) {
                    attackerDamageReceived += blocker.power();
                    if (blocker.lifelink()) {
                        defenderLifeGained += blocker.power();
                    }
                }
            }

            if (attackerDamageReceived >= attacker.toughness() && !attacker.indestructible()) {
                attackerDead = true;
                attackerCreaturesLostValue += attacker.creatureScore();
            }

            // === Regular damage phase ===
            if (!attackerDead && !(attacker.firstStrike() && !attacker.doubleStrike())) {
                int remaining = attacker.power();
                for (CreatureInfo blocker : orderedBlockers) {
                    if (remaining <= 0) break;
                    if (!blocker.indestructible()) {
                        int dmg = Math.min(remaining, blocker.toughness());
                        // Only count blocker death here if attacker didn't have first strike
                        if (dmg >= blocker.toughness() && !attackerDealtFirstStrike) {
                            defenderCreaturesLostValue += blocker.creatureScore();
                        }
                        remaining -= dmg;
                    } else {
                        remaining -= blocker.toughness();
                    }
                }
                if (attacker.trample() && remaining > 0) {
                    if (attacker.infect()) {
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
                    if (!blocker.firstStrike() || blocker.doubleStrike()) {
                        totalRegularBlockerDmg += blocker.power();
                        if (blocker.lifelink() && !(blocker.firstStrike() || blocker.doubleStrike())) {
                            defenderLifeGained += blocker.power();
                        }
                    }
                }
                if (totalRegularBlockerDmg + attackerDamageReceived >= attacker.toughness()
                        && !attacker.indestructible()) {
                    attackerCreaturesLostValue += attacker.creatureScore();
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
                totalPotentialDamage += attacker.power();
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

    static double evaluateBlock(CreatureInfo attacker, CreatureInfo blocker) {
        boolean attackerKillsBlocker = attacker.power() >= blocker.toughness() && !blocker.indestructible();

        if (blocker.power() >= attacker.toughness() && !attacker.indestructible()) {
            if (!attackerKillsBlocker) {
                // Kill attacker, blocker survives — great trade
                return attacker.creatureScore() + 10;
            }
            // Both die — trade evaluation
            return attacker.creatureScore() - blocker.creatureScore();
        }

        if (!attackerKillsBlocker) {
            // Neither dies — still prevents damage
            return attacker.power() * 0.5;
        }

        // Blocker dies, attacker lives — bad unless chump blocking
        return -blocker.creatureScore() + attacker.power() * 0.5;
    }

    /**
     * Evaluates blocking a trample attacker. Unlike evaluateBlock, this factors in how much
     * trample damage the blocker's toughness prevents, preferring high-toughness blockers
     * even when they can't kill the attacker.
     */
    static double evaluateTrampleBlock(CreatureInfo attacker, CreatureInfo blocker) {
        boolean blockerKillsAttacker = blocker.power() >= attacker.toughness() && !attacker.indestructible();
        boolean attackerKillsBlocker = attacker.power() >= blocker.toughness() && !blocker.indestructible();

        if (blockerKillsAttacker) {
            if (!attackerKillsBlocker) {
                // Kill attacker and blocker survives — best outcome, no trample at all
                return attacker.creatureScore() + 10;
            }
            // Both die — trade evaluation
            return attacker.creatureScore() - blocker.creatureScore();
        }

        // Attacker survives with trample: value is based on how much trample damage is prevented.
        // The blocker's toughness is how much of the attacker's power it absorbs.
        int tramplePrevented = Math.min(attacker.power(), blocker.toughness());
        double cost = attackerKillsBlocker ? blocker.creatureScore() : 0;
        return tramplePrevented * 0.8 - cost;
    }

    static double evaluateDefenderBlock(CreatureInfo attacker, CreatureInfo blocker) {
        // From defender's perspective: positive means favorable for the defender
        boolean blockerKillsAttacker = blocker.power() >= attacker.toughness() && !attacker.indestructible();
        boolean attackerKillsBlocker = attacker.power() >= blocker.toughness() && !blocker.indestructible();

        if (blockerKillsAttacker && !attackerKillsBlocker) {
            // Defender kills attacker, blocker survives
            return attacker.creatureScore() + 10;
        }
        if (blockerKillsAttacker && attackerKillsBlocker) {
            // Both die — favorable if attacker is more valuable
            return attacker.creatureScore() - blocker.creatureScore();
        }
        if (!blockerKillsAttacker && !attackerKillsBlocker) {
            // Neither dies — prevents damage
            return attacker.power() * 0.3;
        }
        // Blocker dies, attacker survives — only block if cheap blocker vs expensive attacker
        return -blocker.creatureScore() * 0.5;
    }

    static int[] findBestBlockerPairForMenace(CreatureInfo attacker, List<CreatureInfo> available) {
        int[] bestPair = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (int a = 0; a < available.size(); a++) {
            for (int b = a + 1; b < available.size(); b++) {
                CreatureInfo ba = available.get(a);
                CreatureInfo bb = available.get(b);
                boolean kills = ba.power() + bb.power() >= attacker.toughness();
                boolean oneSurvives = attacker.power() < ba.toughness() || attacker.power() < bb.toughness();

                double value;
                if (kills && oneSurvives) {
                    value = attacker.creatureScore();
                } else if (kills) {
                    value = attacker.creatureScore() - ba.creatureScore() - bb.creatureScore();
                } else {
                    continue;
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestPair = new int[]{ba.index(), bb.index()};
                }
            }
        }

        return bestValue > 0 ? bestPair : null;
    }

    // ===== Counter-Attack Estimation =====

    /**
     * Estimates the outcome of a greedy counter-attack by the opponent given the
     * AI's remaining blockers. Unlike {@link #simulateCombat}, this method models
     * chump blocking: when the total incoming damage would be lethal, the AI
     * blocks even at a negative trade because not blocking would lose the game.
     * <p>
     * {@code blockerAvailable} (nullable = all available) marks which entries of the
     * blocker list may block — used to exclude creatures the AI taps by attacking
     * without copying the blocker list per evaluation.
     * <p>
     * Returns a two-element array: {@code [damageTaken, creaturesLostValue]}.
     */
    static double[] estimateCounterAttackOutcome(List<Attacker> attackers,
                                                 List<CreatureInfo> blockers,
                                                 boolean[] blockerAvailable,
                                                 int aiLife) {
        if (attackers.isEmpty()) return new double[]{0, 0};

        // Total raw incoming damage if we don't block at all (used for lethal-chump check)
        int rawIncoming = 0;
        for (Attacker a : attackers) {
            rawIncoming += a.info().power();
        }

        // Process attackers biggest-first so the AI uses blockers on the largest threats
        List<Attacker> sorted = new ArrayList<>(attackers);
        sorted.sort(Comparator.comparingInt((Attacker a) -> a.info().power()).reversed());

        boolean[] blockerUsed = new boolean[blockers.size()];
        if (blockerAvailable != null) {
            for (int j = 0; j < blockers.size(); j++) {
                blockerUsed[j] = !blockerAvailable[j];
            }
        }
        double damageTaken = 0;
        double creaturesLostValue = 0;
        int remainingIncoming = rawIncoming;

        for (Attacker attacker : sorted) {
            CreatureInfo info = attacker.info();
            int unblockedPower = info.power();

            if (!attacker.blockable()) {
                damageTaken += unblockedPower;
                remainingIncoming -= unblockedPower;
                continue;
            }

            // Find the best single blocker for this attacker
            boolean[] canBeBlockedBy = attacker.canBeBlockedBy();
            int bestIdx = -1;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < blockers.size(); j++) {
                if (blockerUsed[j]) continue;
                if (!canBeBlockedBy[j]) continue;
                CreatureInfo b = blockers.get(j);
                double v = info.trample()
                        ? evaluateTrampleBlock(info, b)
                        : evaluateBlock(info, b);
                if (v > bestValue) {
                    bestValue = v;
                    bestIdx = j;
                }
            }

            boolean mustChump = remainingIncoming >= aiLife;
            if (bestIdx >= 0 && (bestValue > 0 || mustChump)) {
                CreatureInfo b = blockers.get(bestIdx);
                blockerUsed[bestIdx] = true;

                boolean blockerDies = !b.indestructible() && info.power() >= b.toughness();
                if (blockerDies) {
                    creaturesLostValue += b.creatureScore();
                }

                if (info.trample()) {
                    int trampleDmg = Math.max(0, info.power() - b.toughness());
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
     * Computes a penalty reflecting the defensive value we lose by attacking with the
     * creatures in {@code tappedAttackerIds} (the non-vigilance members of the attack
     * subset — they become tapped and cannot block the opponent's next-turn attack).
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
    static double computeDefensiveValuePenalty(Set<UUID> tappedAttackerIds,
                                               List<CreatureInfo> aiPotentialBlockers,
                                               List<Attacker> opponentNextTurnAttackers,
                                               DefensiveBaseline baseline,
                                               int aiLife) {
        if (baseline == null || opponentNextTurnAttackers.isEmpty()) return 0;
        // Vigilance attackers stay untapped and can still block, so they don't
        // reduce our defensive capability.
        if (tappedAttackerIds.isEmpty()) return 0;

        // Remove tapped attackers from the potential blocker pool.
        boolean[] blockerAvailable = new boolean[aiPotentialBlockers.size()];
        int removedCount = 0;
        for (int j = 0; j < aiPotentialBlockers.size(); j++) {
            boolean available = !tappedAttackerIds.contains(aiPotentialBlockers.get(j).id());
            blockerAvailable[j] = available;
            if (!available) removedCount++;
        }
        // If we didn't actually remove anything (e.g. attackers weren't in the
        // potential-blocker set for some reason), the attack doesn't reduce defense.
        if (removedCount == 0) return 0;

        double[] reducedOutcome = estimateCounterAttackOutcome(
                opponentNextTurnAttackers, aiPotentialBlockers, blockerAvailable, aiLife);
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
    static double computeAttackTrickRisk(List<Attacker> attackers,
                                         List<CreatureInfo> blockers,
                                         OpponentThreatEstimator.ThreatEstimate threat) {
        if (!threat.hasThreat()) return 0;
        int pump = threat.estimatedPumpBoost();

        double maxVulnerability = 0;

        for (Attacker attacker : attackers) {
            CreatureInfo info = attacker.info();
            if (!attacker.blockable() || info.indestructible()) continue;

            boolean[] canBeBlockedBy = attacker.canBeBlockedBy();
            for (int j = 0; j < blockers.size(); j++) {
                if (!canBeBlockedBy[j]) continue;
                CreatureInfo blocker = blockers.get(j);

                boolean alreadyLethal = blocker.power() >= info.toughness();
                boolean pumpedLethal = (blocker.power() + pump) >= info.toughness();

                if (pumpedLethal && !alreadyLethal) {
                    // The pump turns a non-lethal block into a lethal one for our attacker.
                    // This is the creature value at risk if the opponent has a trick.
                    maxVulnerability = Math.max(maxVulnerability, info.creatureScore());
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
    static double computeBlockTrickRisk(List<CreatureInfo> attackerInfos,
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

            // A pump on an indestructible attacker can't matter for its own survival,
            // but pumped power could still kill blockers — simulate it either way.
            CreatureInfo pumped = withPump(attackerInfos.get(ai), pump);
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
                c.index(), c.id(), c.perm(),
                c.power() + pump, c.toughness() + pump,
                c.flying(), c.firstStrike(), c.doubleStrike(), c.trample(), c.lifelink(),
                c.indestructible(), c.menace(), c.fear(), c.intimidate(), c.reach(), c.defender(),
                c.cantBeBlocked(), c.isArtifact(), c.infect(), c.color(), c.creatureScore());
    }
}
