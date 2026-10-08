package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesselOfVolatility.class})
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

    @Test
    @DisplayName("Generic mana can pay the generic cost and the mana ability resolves immediately")
    void acceptsColorlessForGenericCostAndDoesNotUseStack() {
        harness.addToBattlefield(player1, new VesselOfVolatility());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Vessel of Volatility");
        harness.assertInGraveyard(player1, "Vessel of Volatility");
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the red cost and a failed activation does not sacrifice the Vessel")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new VesselOfVolatility());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vessel of Volatility");
        harness.assertNotInGraveyard(player1, "Vessel of Volatility");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
