package com.github.laxika.magicalvibes.cards.c;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainMarvelEarthsProtector.class})
class CaptainMarvelEarthsProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up costs only two generic mana during the entry turn")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent captainMarvel = addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("An unsuccessful payment does not consume the power-up activation")
    void failedPaymentDoesNotConsumePowerUp() {
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up cannot be activated again while its first activation is on the stack")
    void activationLimitAppliesBeforeResolution() {
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash allows entry and discounted power-up during an opponent's turn")
    void flashAndPowerUpOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castFromHand(player1, new CaptainMarvelEarthsProtector(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent captainMarvel = findPermanent(player1, "Captain Marvel, Earth's Protector");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up increases combat damage and the life gained from lifelink")
    void poweredUpCombatGainsSixLife() {
        addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
    }
}
