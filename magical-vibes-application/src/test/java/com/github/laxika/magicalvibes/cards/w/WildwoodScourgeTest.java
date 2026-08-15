package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.f.FeralHydra;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildwoodScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new WildwoodScourge()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wildwood Scourge")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gains a counter when another non-Hydra creature you control gets a counter")
    void triggersForAnotherControlledNonHydraCreature() {
        Permanent scourge = castScourgeWithOneCounter();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a Hydra or for a creature controlled by an opponent")
    void ignoresHydrasAndOpponentsCreatures() {
        Permanent scourge = castScourgeWithOneCounter();
        Permanent hydra = castFeralHydraWithOneCounter();

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCard(gd, player1, 0, 0, hydra.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCard(gd, player1, 0, 0, opposingBear.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castScourgeWithOneCounter() {
        harness.setHand(player1, List.of(new WildwoodScourge()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Wildwood Scourge");
    }

    private Permanent castFeralHydraWithOneCounter() {
        harness.setHand(player1, List.of(new FeralHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Feral Hydra");
    }
}
