package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VesselOfVolatilityTest extends BaseCardTest {

    @Test
    @DisplayName("Pays its activation cost, sacrifices itself, and adds four red mana")
    void sacrificesItselfForFourRedMana() {
        harness.addToBattlefield(player1, new VesselOfVolatility());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Vessel of Volatility");
    }

    @Test
    @DisplayName("Cannot activate without two mana including red")
    void requiresOneGenericAndOneRedMana() {
        harness.addToBattlefield(player1, new VesselOfVolatility());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
