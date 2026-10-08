package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.b.BiogenicUpgrade;
import com.github.laxika.magicalvibes.cards.f.FeralHydra;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildwoodScourge.class, BondBeetle.class, FeralHydra.class, GrizzlyBears.class,
        BiogenicUpgrade.class, LlanowarElves.class, SnakeskinVeil.class, WitnessProtection.class})
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
        resolveAllTriggers();

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
        resolveAllTriggers();
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCard(gd, player1, 0, 0, opposingBear.getId(), null);
        resolveAllTriggers();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X zero puts the Scourge into the graveyard")
    void diesWhenCastWithZeroCounters() {
        harness.setHand(player1, List.of(new WildwoodScourge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wildwood Scourge");
        harness.assertInGraveyard(player1, "Wildwood Scourge");
    }

    @Test
    @DisplayName("Counters put on the Scourge itself do not trigger its ability")
    void doesNotTriggerForItself() {
        Permanent scourge = castScourgeWithOneCounter();
        harness.setHand(player1, List.of(new SnakeskinVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, scourge.getId());

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple counters in each of two placement events produce two triggers")
    void triggersOncePerPlacementEventRatherThanPerCounter() {
        Permanent scourge = castScourgeWithOneCounter();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new BiogenicUpgrade()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, Map.of(elves.getId(), 3));
        harness.passBothPriorities();

        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing all abilities prevents new counter-placement triggers")
    void doesNotTriggerWhileEnchantedByWitnessProtection() {
        Permanent scourge = castScourgeWithOneCounter();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, scourge.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new SnakeskinVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, elves.getId());

        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
