package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(1);
        assertThat(lift.isTapped()).isTrue();
    }

    @Test
    void requiresExactManaValueAndMayDecline() {
        Permanent lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 2);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(0);

        lift = addLift();
        lift.setCounterCount(CounterType.WINCH, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 1, 1, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Mercadian Lift");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(lift.getCounterCount(CounterType.WINCH)).isEqualTo(0);
    }

    private Permanent addLift() {
        Permanent lift = new Permanent(new MercadianLift());
        lift.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lift);
        return lift;
    }
}
