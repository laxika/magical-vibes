package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercadianLift.class, FreshVolunteers.class, RamosianSergeant.class})
class MercadianLiftTest extends BaseCardTest {

    @Test
    void addsWinchCounterForOneMana() {
        Permanent lift = addLift();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(1);
        assertThat(lift.isTapped()).isTrue();
    }

    @Test
    void removesXWinchCountersAndPutsMatchingCreatureOntoBattlefield() {
        Permanent lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 3);
        harness.setHand(player1, List.of(new FreshVolunteers(), new RamosianSergeant()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.assertInHand(player1, "Ramosian Sergeant");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(1);
        assertThat(lift.isTapped()).isTrue();
    }

    @Test
    void requiresExactManaValueAndMayDecline() {
        Permanent lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 2);
        harness.setHand(player1, List.of(new RamosianSergeant()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ramosian Sergeant");
        harness.assertInHand(player1, "Ramosian Sergeant");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(0);

        lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 2);
        harness.setHand(player1, List.of(new FreshVolunteers()));

        harness.activateAbility(player1, 1, 1, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Mercadian Lift");
        harness.assertInHand(player1, "Fresh Volunteers");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(0);
    }

    @Test
    void promptsForXWhenTheCounterCostDoesNotAnnounceIt() {
        Permanent lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 2);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(lift.getCounterCount(CounterType.WINCH)).isZero();
        assertThat(lift.isTapped()).isTrue();
    }

    @Test
    void mayChooseZeroWhenTheLiftHasNoWinchCounters() {
        Permanent lift = addLift();
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(lift.getCounterCount(CounterType.WINCH)).isZero();
        assertThat(lift.isTapped()).isTrue();
    }

    @Test
    void cannotRemoveMoreWinchCountersThanTheLiftHas() {
        Permanent lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(1);
        assertThat(lift.isTapped()).isFalse();
    }

    private Permanent addLift() {
        return addCreatureReady(player1, new MercadianLift());
    }
}
