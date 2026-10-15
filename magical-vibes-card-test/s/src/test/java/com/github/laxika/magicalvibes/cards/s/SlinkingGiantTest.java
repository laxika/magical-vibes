package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlinkingGiant.class, CrabappleCohort.class})
class SlinkingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("When Slinking Giant becomes blocked, it gets -3/-0 until end of turn")
    void becomesBlockedGetsMinusThreePower() {
        Permanent giant = addCreatureReady(player1, new SlinkingGiant());
        addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("When Slinking Giant blocks, it gets -3/-0 until end of turn")
    void blocksGetsMinusThreePower() {
        addCreatureReady(player1, new CrabappleCohort());
        Permanent giant = addCreatureReady(player2, new SlinkingGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("If Slinking Giant is unblocked, its combat trigger does not apply")
    void unblockedDoesNotGetMinusThreePower() {
        Permanent giant = addCreatureReady(player1, new SlinkingGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Slinking Giant's combat penalty wears off at end of turn")
    void combatPenaltyWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new SlinkingGiant());
        addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Slinking Giant deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusOneMinusOneCounters() {
        addCreatureReady(player1, new SlinkingGiant());
        Permanent blocker = addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Multiple blockers cause only one -3/-0 penalty")
    void multipleBlockersApplyPenaltyOnce() {
        Permanent giant = addCreatureReady(player1, new SlinkingGiant());
        addCreatureReady(player2, new CrabappleCohort());
        addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(giant.getPowerModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("A blocking Slinking Giant deals one damage as a -1/-1 counter")
    void witherAppliesWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new CrabappleCohort());
        addCreatureReady(player2, new SlinkingGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("An unblocked Slinking Giant deals four ordinary damage to a player")
    void unblockedWitherDealsOrdinaryPlayerDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SlinkingGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
