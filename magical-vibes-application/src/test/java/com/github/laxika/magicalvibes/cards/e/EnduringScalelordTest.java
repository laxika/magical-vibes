package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.f.FeralHydra;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringScalelord.class, BondBeetle.class, Efflorescence.class, FeralHydra.class,
        GrizzlyBears.class})
class EnduringScalelordTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself when another controlled creature gets a counter")
    void triggersForAnotherControlledCreature() {
        Permanent scalelord = castScalelord();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, bears.getId());
        resolveSpellAndTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scalelord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when another controlled Hydra gets a +1/+1 counter")
    void triggersForHydra() {
        Permanent scalelord = castScalelord();
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new FeralHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, hydra.getId());
        resolveSpellAndTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(scalelord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only once when two +1/+1 counters are put on another creature")
    void triggersOnceForMultipleCounters() {
        Permanent scalelord = castScalelord();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        resolveSpellAndTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(scalelord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for itself or a creature controlled by an opponent")
    void ignoresSelfAndOpponentsCreatures() {
        Permanent scalelord = castScalelord();

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, scalelord.getId());
        resolveSpellAndTriggers();
        assertThat(scalelord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, opposingBears.getId());
        resolveSpellAndTriggers();

        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scalelord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castScalelord() {
        harness.setHand(player1, List.of(new EnduringScalelord()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Enduring Scalelord");
    }

    private void resolveSpellAndTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
