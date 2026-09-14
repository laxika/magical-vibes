package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DramatistsPuppet.class, GrizzlyBears.class})
class DramatistsPuppetTest extends BaseCardTest {

    private void setup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DramatistsPuppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("ETB: add one more counter of a kind the target has")
    void addsCounter() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
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
    @DisplayName("ETB: makes an independent decision for each kind of counter")
    void adjustsEachKindIndependently() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "ADD");

        assertThat(bears.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB: a target with no counters does nothing")
    void noCountersNoChoice() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
