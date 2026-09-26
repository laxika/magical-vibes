package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NagaoBoundByHonor.class, MothriderSamurai.class, WanderingOnes.class})
class NagaoBoundByHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Nagao boosts Samurai creatures you control when it attacks")
    void boostsSamuraiCreaturesOnAttack() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());
        Permanent nonSamurai = addCreatureReady(player1, new WanderingOnes());
        Permanent opposingSamurai = addCreatureReady(player2, new MothriderSamurai());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingSamurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSamurai)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nagao's attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido boosts Nagao when it blocks")
    void bushidoWhenBlocking() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent nagao = addCreatureReady(player2, new NagaoBoundByHonor());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nagao's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent nagao = addCreatureReady(player2, new NagaoBoundByHonor());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nagao's attack and Bushido boosts apply when it becomes blocked")
    void bushidoWhenBecomesBlocked() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(5);
    }

    @Test
    @DisplayName("Nagao gets one Bushido bonus when blocked by multiple creatures")
    void bushidoWhenBecomesBlockedByMultipleCreatures() {
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        addCreatureReady(player2, new WanderingOnes());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(5);
    }
}
