package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GallopingLizrogTest extends BaseCardTest {

    @Test
    @DisplayName("ETB removes chosen counters from controlled creatures and doubles them on itself")
    void removesChosenCountersAndDoublesThem() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        firstCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        secondCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        cast();
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");
        assertThat(lizrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Choosing zero counters leaves the battlefield unchanged")
    void choosingZeroCountersDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast();
        harness.handleListChoice(player1, "0");

        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");
        assertThat(lizrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new GallopingLizrog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
