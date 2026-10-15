package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuxaRiverShrine.class})
class LuxaRiverShrineTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gains 1 life and adds a brick counter")
    void firstAbilityGainsLifeAndAddsBrickCounter() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can't be activated with fewer than three brick counters")
    void secondAbilityRequiresThreeBrickCounters() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability gains 2 life with three brick counters")
    void secondAbilityGainsTwoLifeWithThreeBrickCounters() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void firstAbilityDoesNotGainLifeOrAddCountersUntilResolution() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shrine.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isEqualTo(4);
    }

    @Test
    void secondAbilityWorksAboveThresholdWithoutSpendingCounters() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 4);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(shrine.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isEqualTo(4);
    }

    @Test
    void secondAbilityStillResolvesIfCountersAreRemovedAfterActivation() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        shrine.setCounterCount(CounterType.BRICK, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isZero();
    }

    @Test
    void tappingForFirstAbilityPreventsActivatingSecondAbility() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        shrine.setCounterCount(CounterType.BRICK, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
