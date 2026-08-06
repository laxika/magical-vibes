package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BioshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Moves the chosen number of +1/+1 counters onto the second target creature")
    void movesChosenNumberOfCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        cast(source, destination);
        harness.handleListChoice(player1, "2");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moving zero counters leaves both creatures untouched")
    void movingZeroCountersDoesNothing() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(source, destination);
        harness.handleListChoice(player1, "0");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only +1/+1 counters move; other counter kinds stay put")
    void movesOnlyPlusOnePlusOneCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.CHARGE, 2);

        cast(source, destination);
        harness.handleListChoice(player1, "1");

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prompt and does nothing when the first target has no +1/+1 counters")
    void noOpWhenSourceHasNoCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(source, destination);

        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second target must have the same controller as the first")
    void rejectsSecondTargetWithADifferentController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCast();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, List.of(source.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Bioshift()));
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void cast(Permanent source, Permanent destination) {
        prepareCast();
        harness.castInstant(player1, 0, List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
    }
}
