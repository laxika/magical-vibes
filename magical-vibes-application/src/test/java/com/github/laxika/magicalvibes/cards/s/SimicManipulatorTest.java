package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimicManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Removes the chosen number of counters and gains control of a sufficiently small creature")
    void removesChosenCountersAndGainsControl() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(manipulator.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Requires at least one counter and does not pay an illegal activation")
    void requiresAtLeastOneCounter() {
        Permanent manipulator = addReadyManipulator();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("between one");

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Checks the target's power against the chosen counter count")
    void rejectsCreatureTooPowerfulForChosenCount() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than or equal");

        assertThat(manipulator.isTapped()).isFalse();
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles if the target's power is too high when the ability resolves")
    void fizzlesWhenTargetPowerIncreases() {
        Permanent manipulator = addReadyManipulator();
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyManipulator() {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new SimicManipulator());
        manipulator.setSummoningSick(false);
        return manipulator;
    }
}
