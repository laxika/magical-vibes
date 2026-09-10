package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Energizer.class)
class EnergizerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Energizer and puts a +1/+1 counter on it")
    void activationAddsCounter() {
        Permanent energizer = addCreatureReady(player1, new Energizer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(energizer.isTapped()).isTrue();
        assertThat(energizer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(energizer.getEffectivePower()).isEqualTo(3);
        assertThat(energizer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Energizer cannot be activated without the mana")
    void cannotActivateWithoutMana() {
        Permanent energizer = addCreatureReady(player1, new Energizer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(energizer.isTapped()).isFalse();
        assertThat(energizer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Energizer cannot activate its tap ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent energizer = harness.addToBattlefieldAndReturn(player1, new Energizer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(energizer.isTapped()).isFalse();
        assertThat(energizer.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(0);
    }

}
