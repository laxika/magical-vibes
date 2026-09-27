package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ViridianJoiner.class)
class ViridianJoinerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability produces green mana equal to its power")
    void tapProducesGreenManaEqualToPower() {
        addCreatureReady(player1, new ViridianJoiner());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability uses the creature's current power")
    void tapUsesCurrentPower() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        joiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap ability taps the Joiner and cannot be activated again while tapped")
    void tapAbilityRequiresUntappedJoiner() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(joiner.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
