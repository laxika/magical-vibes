package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscendantAcolyte.class, GrizzlyBears.class, SolRing.class})
class AscendantAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each +1/+1 counter on your other creatures")
    void entersWithCountersFromOtherControlledCreatures() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        firstBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        secondBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        opposingBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.setHand(player1, List.of(new AscendantAcolyte()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent acolyte = findPermanent(player1, "Ascendant Acolyte");
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles only its own +1/+1 counters during its controller's upkeep")
    void doublesOwnPlusOneCountersOnUpkeep() {
        Permanent acolyte = addCreatureReady(player1, new AscendantAcolyte());
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        acolyte.setCounterCount(CounterType.CHARGE, 2);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(acolyte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters without counters when there are no other creatures")
    void entersWithoutCountersOnAnEmptyBattlefield() {
        harness.setHand(player1, List.of(new AscendantAcolyte()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ascendant Acolyte")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entry ignores counters on noncreatures and other counter types on creatures")
    void entryCountsOnlyPlusOneCountersOnCreatures() {
        Permanent otherAcolyte = addCreatureReady(player1, new AscendantAcolyte());
        otherAcolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        otherAcolyte.setCounterCount(CounterType.CHARGE, 4);
        Permanent ring = new Permanent(new SolRing());
        ring.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        gd.playerBattlefields.get(player1.getId()).add(ring);

        harness.setHand(player1, List.of(new AscendantAcolyte()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteringAcolyte = findPermanents(player1, "Ascendant Acolyte").get(1);
        assertThat(enteringAcolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherAcolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not double counters during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent acolyte = addCreatureReady(player1, new AscendantAcolyte());
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubling zero counters does not add a counter")
    void doublesZeroCountersToZero() {
        Permanent acolyte = addCreatureReady(player1, new AscendantAcolyte());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep doubling uses the counter count at resolution")
    void doublesCounterCountAtResolution() {
        Permanent acolyte = addCreatureReady(player1, new AscendantAcolyte());
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

}
