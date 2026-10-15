package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GreatFurnace.class)
class GreatFurnaceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Great Furnace adds red mana")
    void tapForRedMana() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(furnace.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Great Furnace cannot produce mana again")
    void cannotActivateWhileTapped() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        assertThat(furnace.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
