package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FerrousLake.class)
class FerrousLakeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one generic mana and tapping adds one blue and one red mana")
    void addsBlueAndRedMana() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new FerrousLake());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(lake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate Ferrous Lake without paying one generic mana")
    void cannotActivateWithoutMana() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new FerrousLake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Ferrous Lake while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new FerrousLake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
