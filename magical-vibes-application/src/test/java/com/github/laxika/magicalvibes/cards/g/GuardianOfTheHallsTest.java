package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfTheHalls.class})
class GuardianOfTheHallsTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {5}{G}{G} to put three +1/+1 counters on itself")
    void putsThreeCountersOnItself() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheHalls());
        guardian.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheHalls());
        guardian.setSummoningSick(true);
        guardian.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations accumulate counters only on their source")
    void repeatedActivationsOnlyPutCountersOnSource() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheHalls());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheHalls());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GuardianOfTheHalls());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot replace the second green mana with generic mana")
    void requiresTwoGreenMana() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheHalls());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
