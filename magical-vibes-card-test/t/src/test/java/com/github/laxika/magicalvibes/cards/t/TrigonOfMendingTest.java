package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TrigonOfMending.class, Shatter.class})
class TrigonOfMendingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfMending()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = findPermanent(player1, "Trigon of Mending");
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }


    @Test
    @DisplayName("First ability adds a charge counter")
    void firstAbilityAddsChargeCounter() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Second ability removes a charge counter and target player gains 3 life")
    void secondAbilityGainsLife() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife + 3);
    }

    @Test
    @DisplayName("Can target opponent to gain life")
    void canTargetOpponentToGainLife() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 1);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife + 3);
    }

    @Test
    @DisplayName("Can activate second ability multiple times with enough counters (untapping between)")
    void canActivateMultipleTimes() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife + 6);
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Recharging requires two white mana")
    void rechargeRequiresWhiteMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trigon.isTapped()).isFalse();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Recharging an empty trigon taps it immediately and adds a counter on resolution")
    void rechargeEmptyTrigonUsesStackAndTapCost() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gain removes the charge counter as a cost before resolution")
    void lifeGainPaysCounterCostBeforeResolution() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        int initialLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, player1.getId());

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player1, initialLife);

        harness.passBothPriorities();

        harness.assertLife(player1, initialLife + 3);
    }

    @Test
    @DisplayName("Life gain resolves even if the trigon is destroyed in response")
    void lifeGainResolvesAfterSourceIsDestroyed() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfMending());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        int initialLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, player1.getId());

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, trigon.getId());

        harness.assertNotOnBattlefield(player1, "Trigon of Mending");
        harness.assertLife(player1, initialLife);
        harness.passBothPriorities();
        harness.assertLife(player1, initialLife + 3);
    }
}
