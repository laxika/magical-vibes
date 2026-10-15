package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Hazard of the Dunes")
@CardUsed({HazardOfTheDunes.class})
class HazardOfTheDunesTest extends BaseCardTest {

    @Test
    @DisplayName("The exhaust ability puts three +1/+1 counters on Hazard of the Dunes")
    void exhaustPutsThreeCountersOnIt() {
        Permanent hazard = harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hazard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        addExhaustMana(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust is used before its activation resolves")
    void cannotActivateAgainBeforeResolution() {
        Permanent hazard = harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        addExhaustMana(2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hazard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();
        assertThat(hazard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each copy has its own exhaust activation")
    void separateCopiesCanEachExhaust() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        addExhaustMana(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exhaust can be activated while the creature is tapped")
    void tappedCreatureCanExhaust() {
        Permanent hazard = harness.addToBattlefieldAndReturn(player1, new HazardOfTheDunes());
        hazard.tap();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hazard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hazard.isTapped()).isTrue();
    }

    private void addExhaustMana() {
        addExhaustMana(1);
    }

    private void addExhaustMana(int multiplier) {
        harness.addMana(player1, ManaColor.GREEN, multiplier);
        harness.addMana(player1, ManaColor.COLORLESS, 6 * multiplier);
    }
}
