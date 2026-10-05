package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Prowcatcher Specialist")
@CardUsed({ProwcatcherSpecialist.class})
class ProwcatcherSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts two +1/+1 counters on it")
    void exhaustPutsTwoCountersOnIt() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new ProwcatcherSpecialist());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, specialist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, specialist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new ProwcatcherSpecialist());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust is already used while its ability is on the stack")
    void cannotActivateAgainBeforeResolution() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new ProwcatcherSpecialist());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();
        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Specialist can exhaust independently and only counters itself")
    void separateCopiesExhaustIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ProwcatcherSpecialist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ProwcatcherSpecialist());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust can be activated while the Specialist is tapped")
    void exhaustDoesNotRequireUntappedSource() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new ProwcatcherSpecialist());
        specialist.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(specialist.isTapped()).isTrue();
    }
}
