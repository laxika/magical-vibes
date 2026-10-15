package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TrigonOfThought.class, Shatter.class})
class TrigonOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.castFromHand(player1, new TrigonOfThought(), "{5}");
        harness.passBothPriorities();

        Permanent trigon = findPermanent(player1, "Trigon of Thought");
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability adds a charge counter")
    void firstAbilityAddsChargeCounter() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability removes a charge counter and draws a card")
    void secondAbilityDrawsCard() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(initialHandSize + 1);
    }

    @Test
    @DisplayName("Can activate second ability multiple times with enough counters (untapping between)")
    void canActivateMultipleTimes() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(initialHandSize + 2);
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Charging taps immediately but adds its counter only on resolution")
    void chargingUsesTheStackAndWorksWithoutExistingCounters() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Charging requires two blue mana and cannot use generic mana instead")
    void chargingRequiresBlueMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.isTapped()).isFalse();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Charging cannot be activated while tapped")
    void cannotChargeWhileTapped() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The last charge counter is paid immediately and drawing waits for resolution")
    void lastCounterIsAnActivationCost() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of());
        TrigonOfThought drawnCard = new TrigonOfThought();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Drawing still resolves after the Trigon is destroyed in response")
    void drawingSurvivesSourceDestruction() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of());
        TrigonOfThought drawnCard = new TrigonOfThought();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, trigon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trigon);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Drawing requires two mana even with an available charge counter")
    void cannotDrawWithInsufficientMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfThought());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.isTapped()).isFalse();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
