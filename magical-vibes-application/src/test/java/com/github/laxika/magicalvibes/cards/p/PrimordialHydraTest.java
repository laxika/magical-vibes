package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimordialHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=4 enters with four +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new PrimordialHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4); // 4 generic for X

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Primordial Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting with X=0 enters as a 0/0 and dies")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new PrimordialHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primordial Hydra");
    }

    @Test
    @DisplayName("Upkeep trigger doubles the +1/+1 counters")
    void upkeepDoublesCounters() {
        harness.addToBattlefield(player1, new PrimordialHydra());
        Permanent hydra = findPermanent(player1, "Primordial Hydra");
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the doubling trigger

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire on an opponent's upkeep")
    void doesNotDoubleOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PrimordialHydra());
        Permanent hydra = findPermanent(player1, "Primordial Hydra");
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("No trample below ten +1/+1 counters, trample at ten or more")
    void trampleOnlyAtTenCounters() {
        harness.addToBattlefield(player1, new PrimordialHydra());
        Permanent hydra = findPermanent(player1, "Primordial Hydra");

        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.TRAMPLE)).isFalse();

        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.TRAMPLE)).isTrue();
    }
}
