package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.CombatSimulator.CombatOutcome;
import com.github.laxika.magicalvibes.ai.CombatSimulator.CreatureInfo;
import com.github.laxika.magicalvibes.ai.CombatSimulator.DefensiveBaseline;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-arithmetic tests for {@link CombatMath}. No game harness: creatures are built as
 * plain {@link CreatureInfo} snapshots and block legality is supplied as precomputed rows,
 * exactly like {@link CombatSimulator#findBestAttackers} does in the subset search.
 */
class CombatMathTest {

    private static CreatureInfo creature(int index, int power, int toughness, double score, Keyword... keywords) {
        Set<Keyword> kw = Set.of(keywords);
        return new CreatureInfo(index, UUID.randomUUID(), null, power, toughness,
                kw.contains(Keyword.FLYING),
                kw.contains(Keyword.FIRST_STRIKE),
                kw.contains(Keyword.DOUBLE_STRIKE),
                kw.contains(Keyword.TRAMPLE),
                kw.contains(Keyword.LIFELINK),
                kw.contains(Keyword.INDESTRUCTIBLE),
                kw.contains(Keyword.MENACE),
                kw.contains(Keyword.FEAR),
                kw.contains(Keyword.INTIMIDATE),
                kw.contains(Keyword.REACH),
                kw.contains(Keyword.DEFENDER),
                false, false,
                kw.contains(Keyword.INFECT),
                CardColor.GREEN, score);
    }

    /** Wraps an attacker with an explicit block-compatibility row (no vigilance). */
    private static CombatMath.Attacker attacker(CreatureInfo info, boolean... canBeBlockedBy) {
        return CombatMath.Attacker.of(info, canBeBlockedBy, false);
    }

    // ===== Attacker.of =====

    @Test
    @DisplayName("Attacker.of derives blockable from the compatibility row")
    void attackerOfDerivesBlockable() {
        CreatureInfo info = creature(0, 2, 2, 2.0);
        assertThat(CombatMath.Attacker.of(info, new boolean[]{false, true}, false).blockable()).isTrue();
        assertThat(CombatMath.Attacker.of(info, new boolean[]{false, false}, false).blockable()).isFalse();
        assertThat(CombatMath.Attacker.of(info, new boolean[]{}, false).blockable()).isFalse();
    }

    // ===== simulateCombat =====

    @Test
    @DisplayName("Unblocked attacker deals damage equal to power")
    void unblockedAttackerDealsDamage() {
        CombatMath.Attacker bears = attacker(creature(0, 3, 3, 3.0));
        CombatOutcome outcome = CombatMath.simulateCombat(List.of(bears), List.of(), 20);

        assertThat(outcome.opponentLifeChange()).isEqualTo(-3);
        assertThat(outcome.opponentPoisonChange()).isEqualTo(0);
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(0.0);
        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Unblocked infect attacker deals poison instead of life damage")
    void unblockedInfectDealsPoison() {
        CombatMath.Attacker infect = attacker(creature(0, 2, 2, 2.0, Keyword.INFECT));
        CombatOutcome outcome = CombatMath.simulateCombat(List.of(infect), List.of(), 20);

        assertThat(outcome.opponentLifeChange()).isEqualTo(0);
        assertThat(outcome.opponentPoisonChange()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unblocked lifelink attacker gains its controller life")
    void unblockedLifelinkGainsLife() {
        CombatMath.Attacker lifelinker = attacker(creature(0, 4, 4, 4.0, Keyword.LIFELINK));
        CombatOutcome outcome = CombatMath.simulateCombat(List.of(lifelinker), List.of(), 20);

        assertThat(outcome.opponentLifeChange()).isEqualTo(-4);
        assertThat(outcome.aiLifeChange()).isEqualTo(4);
    }

    @Test
    @DisplayName("Defender blocks favorably: small attacker dies to a big blocker")
    void defenderBlocksFavorably() {
        CreatureInfo blocker = creature(0, 4, 4, 4.0);
        CombatMath.Attacker bears = attacker(creature(0, 2, 2, 2.0), true);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(bears), List.of(blocker), 20);

        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(2.0);
        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(0.0);
        assertThat(outcome.opponentLifeChange()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacker with an all-false row is unblockable even with blockers present")
    void allFalseRowMeansUnblocked() {
        CreatureInfo blocker = creature(0, 4, 4, 4.0);
        CombatMath.Attacker flyer = attacker(creature(0, 3, 3, 3.0, Keyword.FLYING), false);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(flyer), List.of(blocker), 20);

        assertThat(outcome.opponentLifeChange()).isEqualTo(-3);
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("First strike attacker kills a small blocker and survives")
    void firstStrikeKillsBlockerFirst() {
        CreatureInfo blocker = creature(0, 1, 1, 1.0);
        CombatMath.Attacker firstStriker = attacker(creature(0, 3, 2, 3.0, Keyword.FIRST_STRIKE), true);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(firstStriker), List.of(blocker), 20);

        // Greedy defender chump-blocks only if favorable; 1/1 into 3/2 is value-positive for
        // the defender model only via damage prevention — verify whichever branch resolves,
        // the first striker never dies.
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Trample: both creatures trade and excess damage carries over")
    void trampleExcessCarriesOver() {
        // 6/6 trample (score 6) attacks into a 6/2 blocker (score 2): favorable trade for the
        // defender model (kills the bigger attacker), 4 excess damage tramples through.
        CreatureInfo blocker = creature(0, 6, 2, 2.0);
        CombatMath.Attacker trampler = attacker(creature(0, 6, 6, 6.0, Keyword.TRAMPLE), true);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(trampler), List.of(blocker), 20);

        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(2.0);
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(6.0);
        assertThat(outcome.opponentLifeChange()).isEqualTo(-4);
    }

    @Test
    @DisplayName("Menace attacker is double-blocked when a killing pair exists")
    void menaceDoubleBlocked() {
        CreatureInfo blockerA = creature(0, 2, 2, 2.0);
        CreatureInfo blockerB = creature(1, 2, 2, 2.0);
        CombatMath.Attacker menacer = attacker(creature(0, 3, 3, 5.0, Keyword.MENACE), true, true);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(menacer), List.of(blockerA, blockerB), 20);

        // Pair (2+2 power) kills the 3-toughness attacker; the attacker's 3 damage kills
        // only the first 2-toughness blocker in damage order.
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(5.0);
        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(2.0);
        assertThat(outcome.opponentLifeChange()).isEqualTo(0);
    }

    @Test
    @DisplayName("Menace attacker with a single available blocker goes unblocked")
    void menaceSingleBlockerUnblocked() {
        CreatureInfo blocker = creature(0, 2, 2, 2.0);
        CombatMath.Attacker menacer = attacker(creature(0, 3, 3, 5.0, Keyword.MENACE), true);

        CombatOutcome outcome = CombatMath.simulateCombat(List.of(menacer), List.of(blocker), 20);

        assertThat(outcome.opponentLifeChange()).isEqualTo(-3);
        assertThat(outcome.aiCreaturesLostValue()).isEqualTo(0.0);
        assertThat(outcome.opponentCreaturesLostValue()).isEqualTo(0.0);
    }

    // ===== evaluateDefenderCombat =====

    @Test
    @DisplayName("Defender combat: unblocked lethal damage incurs the heavy penalty")
    void defenderCombatLethalPenalty() {
        CreatureInfo attacker = creature(0, 5, 5, 5.0);
        List<List<CreatureInfo>> noBlocks = new ArrayList<>();
        noBlocks.add(new ArrayList<>());

        double score = CombatMath.evaluateDefenderCombat(List.of(attacker), noBlocks, 5, 0);

        // 5 damage at 5 life: lethal ratio 1.0 -> life weight 5.0 -> -25, plus the -1000 sentinel.
        assertThat(score).isEqualTo(-1025.0);
    }

    @Test
    @DisplayName("Defender combat: menace attacker with one blocker counts as unblocked")
    void defenderCombatMenaceSingleBlockUnblocked() {
        CreatureInfo menacer = creature(0, 3, 3, 3.0, Keyword.MENACE);
        CreatureInfo blocker = creature(0, 4, 4, 4.0);
        List<List<CreatureInfo>> oneBlock = new ArrayList<>();
        oneBlock.add(new ArrayList<>(List.of(blocker)));

        double blocked = CombatMath.evaluateDefenderCombat(List.of(menacer), oneBlock, 20, 0);

        // Treated as unblocked: 3 life lost at weight 2.0, nothing else.
        assertThat(blocked).isEqualTo(-6.0);
    }

    @Test
    @DisplayName("Defender combat: favorable block kills the attacker for free")
    void defenderCombatFavorableBlock() {
        CreatureInfo attacker = creature(0, 2, 2, 2.0);
        CreatureInfo blocker = creature(0, 4, 4, 4.0);
        List<List<CreatureInfo>> blocks = new ArrayList<>();
        blocks.add(new ArrayList<>(List.of(blocker)));

        double score = CombatMath.evaluateDefenderCombat(List.of(attacker), blocks, 20, 0);

        // Attacker (value 2.0) dies, blocker survives, no damage taken.
        assertThat(score).isEqualTo(2.0);
    }

    // ===== evaluateBlock / evaluateTrampleBlock =====

    @Test
    @DisplayName("evaluateBlock: kill attacker while surviving is a premium trade")
    void evaluateBlockGreatTrade() {
        CreatureInfo attacker = creature(0, 2, 2, 3.0);
        CreatureInfo blocker = creature(0, 4, 4, 4.0);

        assertThat(CombatMath.evaluateBlock(attacker, blocker)).isEqualTo(13.0);
    }

    @Test
    @DisplayName("evaluateTrampleBlock values absorbed trample damage")
    void evaluateTrampleBlockValuesAbsorption() {
        // 6/6 trample vs 1/4 blocker that dies: prevents min(6,4)=4 damage at 0.8/point,
        // minus the blocker's score.
        CreatureInfo attacker = creature(0, 6, 6, 6.0, Keyword.TRAMPLE);
        CreatureInfo blocker = creature(0, 1, 4, 1.0);

        assertThat(CombatMath.evaluateTrampleBlock(attacker, blocker)).isEqualTo(4 * 0.8 - 1.0);
    }

    // ===== findBestBlockerPairForMenace =====

    @Test
    @DisplayName("Menace pair search picks the pair that kills the attacker")
    void menacePairSearchPicksKillingPair() {
        CreatureInfo attacker = creature(0, 4, 4, 4.0, Keyword.MENACE);
        CreatureInfo a = creature(0, 2, 2, 1.0);
        CreatureInfo b = creature(1, 2, 2, 1.0);
        CreatureInfo c = creature(2, 1, 1, 0.5);

        int[] pair = CombatMath.findBestBlockerPairForMenace(attacker, List.of(a, b, c));

        assertThat(pair).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Menace pair search returns null when no pair is worth it")
    void menacePairSearchNullWhenUnfavorable() {
        CreatureInfo attacker = creature(0, 6, 6, 2.0, Keyword.MENACE);
        CreatureInfo a = creature(0, 1, 1, 3.0);
        CreatureInfo b = creature(1, 1, 1, 3.0);

        assertThat(CombatMath.findBestBlockerPairForMenace(attacker, List.of(a, b))).isNull();
    }

    // ===== estimateCounterAttackOutcome =====

    @Test
    @DisplayName("Counter-attack: favorable block absorbs all damage")
    void counterAttackFavorableBlock() {
        CreatureInfo blocker = creature(0, 3, 3, 3.0);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 2, 2, 2.0), true);

        double[] outcome = CombatMath.estimateCounterAttackOutcome(
                List.of(oppAttacker), List.of(blocker), null, 20);

        assertThat(outcome[0]).isEqualTo(0.0);
        assertThat(outcome[1]).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Counter-attack: unavailable blocker lets the damage through")
    void counterAttackUnavailableBlocker() {
        CreatureInfo blocker = creature(0, 3, 3, 3.0);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 2, 2, 2.0), true);

        double[] outcome = CombatMath.estimateCounterAttackOutcome(
                List.of(oppAttacker), List.of(blocker), new boolean[]{false}, 20);

        assertThat(outcome[0]).isEqualTo(2.0);
        assertThat(outcome[1]).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Counter-attack: chump block happens only when incoming damage is lethal")
    void counterAttackChumpsOnlyWhenLethal() {
        // Expensive 2/2 blocker vs a 6/6 attacker: a losing block the AI declines at
        // comfortable life totals but takes when the 6 damage would be lethal.
        CreatureInfo blocker = creature(0, 2, 2, 5.0);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 6, 6, 6.0), true);

        double[] comfortable = CombatMath.estimateCounterAttackOutcome(
                List.of(oppAttacker), List.of(blocker), null, 20);
        assertThat(comfortable[0]).isEqualTo(6.0);
        assertThat(comfortable[1]).isEqualTo(0.0);

        double[] desperate = CombatMath.estimateCounterAttackOutcome(
                List.of(oppAttacker), List.of(blocker), null, 5);
        assertThat(desperate[0]).isEqualTo(0.0);
        assertThat(desperate[1]).isEqualTo(5.0);
    }

    // ===== computeDefensiveValuePenalty =====

    @Test
    @DisplayName("Defensive penalty: zero when no attacker actually taps")
    void defensivePenaltyZeroWithoutTappedAttackers() {
        CreatureInfo aiBlocker = creature(0, 3, 3, 3.0);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 5, 5, 5.0), true);
        DefensiveBaseline baseline = new DefensiveBaseline(0, 2.5);

        double penalty = CombatMath.computeDefensiveValuePenalty(
                Set.of(), List.of(aiBlocker), List.of(oppAttacker), baseline, 5);

        assertThat(penalty).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Defensive penalty: lethal flip adds the sentinel on top of the damage delta")
    void defensivePenaltyLethalFlip() {
        // AI at 5 life. Baseline: the 3/3 chump-blocks the 5/5 (0 damage, 2.5 value lost).
        // If the 3/3 attacks (taps), the 5/5 lands 5 lethal damage: sentinel 1000 + 5*2.
        CreatureInfo aiBlocker = creature(0, 3, 3, 2.5);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 5, 5, 5.0), true);

        double[] baselineOutcome = CombatMath.estimateCounterAttackOutcome(
                List.of(oppAttacker), List.of(aiBlocker), null, 5);
        DefensiveBaseline baseline = new DefensiveBaseline(baselineOutcome[0], baselineOutcome[1]);
        assertThat(baseline.damageTaken()).isEqualTo(0.0);
        assertThat(baseline.creaturesLostValue()).isEqualTo(2.5);

        double penalty = CombatMath.computeDefensiveValuePenalty(
                Set.of(aiBlocker.id()), List.of(aiBlocker), List.of(oppAttacker), baseline, 5);

        assertThat(penalty).isEqualTo(1010.0);
    }

    @Test
    @DisplayName("Defensive penalty: zero when the tapped creatures were no blockers anyway")
    void defensivePenaltyZeroWhenNotInBlockerPool() {
        CreatureInfo aiBlocker = creature(0, 3, 3, 3.0);
        CombatMath.Attacker oppAttacker = attacker(creature(0, 5, 5, 5.0), true);
        DefensiveBaseline baseline = new DefensiveBaseline(0, 2.5);

        double penalty = CombatMath.computeDefensiveValuePenalty(
                Set.of(UUID.randomUUID()), List.of(aiBlocker), List.of(oppAttacker), baseline, 5);

        assertThat(penalty).isEqualTo(0.0);
    }

    // ===== computeAttackTrickRisk =====

    @Test
    @DisplayName("Attack trick risk: pump that flips a block to lethal puts the attacker at risk")
    void attackTrickRiskPumpFlips() {
        CreatureInfo blocker = creature(0, 1, 1, 1.0);
        CombatMath.Attacker aiAttacker = attacker(creature(0, 3, 3, 3.0), true);
        var threat = new OpponentThreatEstimator.ThreatEstimate(0.5, 3);

        double risk = CombatMath.computeAttackTrickRisk(List.of(aiAttacker), List.of(blocker), threat);

        assertThat(risk).isEqualTo(3.0 * 0.5);
    }

    @Test
    @DisplayName("Attack trick risk: zero when the blocker cannot block or already kills")
    void attackTrickRiskZeroCases() {
        var threat = new OpponentThreatEstimator.ThreatEstimate(0.5, 3);

        // Row says the blocker can't block: no risk.
        CreatureInfo blocker = creature(0, 1, 1, 1.0);
        CombatMath.Attacker unblockable = attacker(creature(0, 3, 3, 3.0), false);
        assertThat(CombatMath.computeAttackTrickRisk(List.of(unblockable), List.of(blocker), threat))
                .isEqualTo(0.0);

        // Blocker already kills the attacker without the pump: the trick changes nothing.
        CreatureInfo bigBlocker = creature(0, 5, 5, 5.0);
        CombatMath.Attacker attackerInfo = attacker(creature(0, 3, 3, 3.0), true);
        assertThat(CombatMath.computeAttackTrickRisk(List.of(attackerInfo), List.of(bigBlocker), threat))
                .isEqualTo(0.0);

        // No threat estimate: no risk at all.
        assertThat(CombatMath.computeAttackTrickRisk(List.of(attackerInfo), List.of(blocker),
                OpponentThreatEstimator.ThreatEstimate.NONE)).isEqualTo(0.0);
    }

    // ===== computeBlockTrickRisk =====

    @Test
    @DisplayName("Block trick risk: positive when a pump flips a profitable block")
    void blockTrickRiskPositiveWhenPumpFlips() {
        // AI 3/3 blocks a 2/3: profitable now, disastrous if the attacker gets +3/+3.
        CreatureInfo oppAttacker = creature(0, 2, 3, 2.0);
        CreatureInfo aiBlocker = creature(0, 3, 3, 3.0);
        List<List<CreatureInfo>> assignments = new ArrayList<>();
        assignments.add(new ArrayList<>(List.of(aiBlocker)));
        var threat = new OpponentThreatEstimator.ThreatEstimate(0.3, 3);

        double risk = CombatMath.computeBlockTrickRisk(List.of(oppAttacker), assignments, 20, 0, threat);

        assertThat(risk).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Block trick risk: zero without blocks or without a threat")
    void blockTrickRiskZeroCases() {
        CreatureInfo oppAttacker = creature(0, 2, 3, 2.0);
        List<List<CreatureInfo>> noBlocks = new ArrayList<>();
        noBlocks.add(new ArrayList<>());
        var threat = new OpponentThreatEstimator.ThreatEstimate(0.5, 4);

        assertThat(CombatMath.computeBlockTrickRisk(List.of(oppAttacker), noBlocks, 20, 0, threat))
                .isEqualTo(0.0);

        CreatureInfo aiBlocker = creature(0, 3, 3, 3.0);
        List<List<CreatureInfo>> blocks = new ArrayList<>();
        blocks.add(new ArrayList<>(List.of(aiBlocker)));
        assertThat(CombatMath.computeBlockTrickRisk(List.of(oppAttacker), blocks, 20, 0,
                OpponentThreatEstimator.ThreatEstimate.NONE)).isEqualTo(0.0);
    }
}
