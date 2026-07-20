package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuarryHaulerTest extends BaseCardTest {

    private void setup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QuarryHauler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("ETB: add one more counter of a kind the target has")
    void addsCounter() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB — awaits add/remove choice
        harness.handleListChoice(player1, "ADD");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB: remove one counter of a kind the target has")
    void removesCounter() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB: an independent add/remove decision is made for each kind of counter")
    void adjustsEachKindIndependently() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // CHARGE precedes PLUS_ONE_PLUS_ONE in the counter-kind order, so it is prompted first.
        bears.setCounterCount(CounterType.CHARGE, 1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE"); // charge counter
        harness.handleListChoice(player1, "ADD");    // +1/+1 counters

        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB: a target with no counters does nothing and asks for no choice")
    void noCountersNoChoice() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }
}
