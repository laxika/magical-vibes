package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bioshift.class, ScabClanCharger.class, SimicGuildgate.class, Solemnity.class})
class BioshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Moves the chosen number of +1/+1 counters onto the second target creature")
    void movesChosenNumberOfCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        cast(source, destination);
        harness.handleListChoice(player1, "2");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moving zero counters leaves both creatures untouched")
    void movingZeroCountersDoesNothing() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(source, destination);
        harness.handleListChoice(player1, "0");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only +1/+1 counters move; other counter kinds stay put")
    void movesOnlyPlusOnePlusOneCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
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
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());

        cast(source, destination);

        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second target must have the same controller as the first")
    void rejectsSecondTargetWithADifferentController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCast();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, List.of(source.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canMoveAllCountersBetweenOpponentsCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        destination.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(source, destination);
        harness.handleListChoice(player1, "3");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void rejectsSameCreatureForBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotMoveCountersWhenDestinationLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCast();
        harness.castInstant(player1, 0, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(destination);
        gd.playerGraveyards.get(player1.getId()).add(destination.getCard());

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stillMovesCountersWhenBothTargetsChangeToTheSameNewController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCast();
        harness.castInstant(player1, 0, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(source, destination));
        gd.playerBattlefields.get(player2.getId()).addAll(List.of(source, destination));

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "2");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotRemoveCountersWhenDestinationCannotReceiveThem() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ScabClanCharger());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new Solemnity());

        cast(source, destination);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player1, "2");
        }

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Bioshift()));
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void cast(Permanent source, Permanent destination) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), destination.getId()));
    }
}
