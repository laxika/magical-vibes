package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({Pyroceratops.class, Shock.class, GrizzlyBears.class})
class PyroceratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Pyroceratops")
    void noncreatureSpellPutsCounterOnSource() {
        Permanent pyroceratops = harness.addToBattlefieldAndReturn(player1, new Pyroceratops());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyroceratops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, pyroceratops)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pyroceratops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not put a counter on Pyroceratops")
    void creatureSpellDoesNotPutCounterOnSource() {
        Permanent pyroceratops = harness.addToBattlefieldAndReturn(player1, new Pyroceratops());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(pyroceratops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not put a counter on Pyroceratops")
    void opponentNoncreatureSpellDoesNotPutCounterOnSource() {
        Permanent pyroceratops = harness.addToBattlefieldAndReturn(player1, new Pyroceratops());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(pyroceratops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
