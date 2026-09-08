package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorceressQueen.class, GrizzlyBears.class, Forest.class})
class SorceressQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sets target creature's base power and toughness to 0/2")
    void setsTargetBasePowerToughness() {
        Permanent queen = addCreatureReady(player1, new SorceressQueen());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(queen.isTapped()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(0);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Base power/toughness override wears off at cleanup")
    void wearsOffAtCleanup() {
        addCreatureReady(player1, new SorceressQueen());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Base power and toughness setting leaves counters applied")
    void leavesCountersApplied() {
        addCreatureReady(player1, new SorceressQueen());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new SorceressQueen());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target itself with the ability")
    void cannotTargetItself() {
        Permanent queen = addCreatureReady(player1, new SorceressQueen());
        // Another legal creature target so the ability is activatable at all.
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, queen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
