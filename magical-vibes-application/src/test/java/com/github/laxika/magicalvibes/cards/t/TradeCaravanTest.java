package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LandCap;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradeCaravan.class, Plains.class, LandCap.class})
class TradeCaravanTest extends BaseCardTest {

    private void enterOpponentUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Your upkeep puts a currency counter on Trade Caravan")
    void upkeepAddsCurrencyCounter() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's upkeep does not add a currency counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isZero();
    }

    @Test
    @DisplayName("Removing two currency counters untaps target basic land during an opponent's upkeep")
    void untapsBasicLandDuringOpponentUpkeep() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 3);
        plains.tap();

        enterOpponentUpkeep();
        harness.activateAbility(player1, 0, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(plains.isTapped()).isFalse();
        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing currency counters is an activation cost")
    void removesCurrencyCountersOnActivation() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 2);
        plains.tap();

        enterOpponentUpkeep();
        harness.activateAbility(player1, 0, 0, null, plains.getId());

        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isZero();
        assertThat(plains.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(plains.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an opponent's basic land without tapping Trade Caravan")
    void untapsOpponentsBasicLandWithoutTappingCaravan() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 2);
        caravan.tap();
        plains.tap();

        enterOpponentUpkeep();
        harness.activateAbility(player1, 0, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(plains.isTapped()).isFalse();
        assertThat(caravan.isTapped()).isTrue();
        assertThat(caravan.getCounterCount(CounterType.CURRENCY)).isZero();
    }

    @Test
    @DisplayName("Cannot be activated during your own upkeep")
    void cannotActivateDuringOwnUpkeep() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 2);
        plains.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        assertThat(plains.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot be activated outside an upkeep step")
    void cannotActivateOutsideUpkeep() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 2);
        plains.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated with only one currency counter")
    void requiresTwoCounters() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        caravan.setCounterCount(CounterType.CURRENCY, 1);
        plains.tap();

        enterOpponentUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plains.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonbasic land")
    void cannotTargetNonbasicLand() {
        Permanent caravan = harness.addToBattlefieldAndReturn(player1, new TradeCaravan());
        Permanent landCap = harness.addToBattlefieldAndReturn(player1, new LandCap());
        caravan.setCounterCount(CounterType.CURRENCY, 2);
        landCap.tap();

        enterOpponentUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, landCap.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(landCap.isTapped()).isTrue();
    }
}
