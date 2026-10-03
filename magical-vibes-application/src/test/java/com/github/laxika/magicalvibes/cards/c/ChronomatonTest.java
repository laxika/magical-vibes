package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chronomaton.class})
class ChronomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a +1/+1 counter on Chronomaton and taps it")
    void activationAddsCounter() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(golem.isTapped()).isTrue();
        assertThat(golem.getEffectivePower()).isEqualTo(2);
        assertThat(golem.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters accumulate across activations")
    void countersAccumulate() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        golem.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(golem.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chronomaton cannot activate its ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());
        golem.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("The tap cost is paid immediately but the counter waits for resolution")
    void counterWaitsForResolution() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(golem.isTapped()).isTrue();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());
        golem.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(golem.isTapped()).isFalse();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating requires one mana")
    void cannotActivateWithoutMana() {
        Permanent golem = addCreatureReady(player1, new Chronomaton());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(golem.isTapped()).isFalse();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
