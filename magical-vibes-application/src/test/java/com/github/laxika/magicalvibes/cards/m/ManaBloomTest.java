package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three charge counters")
    void entersWithXChargeCounters() {
        harness.setHand(player1, List.of(new ManaBloom()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mana Bloom").getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a charge counter adds one mana of the chosen color")
    void removingCounterAddsAnyColorMana() {
        Permanent bloom = harness.addToBattlefieldAndReturn(player1, new ManaBloom());
        bloom.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(bloom.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability can only be activated once each turn")
    void abilityIsOncePerTurn() {
        Permanent bloom = harness.addToBattlefieldAndReturn(player1, new ManaBloom());
        bloom.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bloom.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can't be activated without a charge counter")
    void abilityNeedsChargeCounter() {
        harness.addToBattlefield(player1, new ManaBloom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep trigger returns Mana Bloom to hand when it has no charge counters")
    void upkeepReturnsToHandWithNoCounters() {
        harness.addToBattlefield(player1, new ManaBloom());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(countPermanents(player1, "Mana Bloom")).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> "Mana Bloom".equals(card.getName()));
    }

    @Test
    @DisplayName("Upkeep trigger leaves Mana Bloom on the battlefield while it has charge counters")
    void upkeepDoesNothingWithCounters() {
        Permanent bloom = harness.addToBattlefieldAndReturn(player1, new ManaBloom());
        bloom.setCounterCount(CounterType.CHARGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mana Bloom")).isEqualTo(1);
    }
}
