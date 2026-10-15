package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DarkwaterCatacombs.class)
class DarkwaterCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one generic mana and tapping adds one blue and one black mana")
    void addsBlueAndBlackMana() {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new DarkwaterCatacombs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(catacombs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate Darkwater Catacombs without paying one generic mana")
    void cannotActivateWithoutMana() {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new DarkwaterCatacombs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(catacombs.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Darkwater Catacombs while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new DarkwaterCatacombs());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Any colored mana can pay the generic activation cost")
    void coloredManaPaysGenericCost(ManaColor paymentColor) {
        Permanent catacombs = harness.addToBattlefieldAndReturn(player1, new DarkwaterCatacombs());
        harness.addMana(player1, paymentColor, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(catacombs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered land can produce mana immediately without using the stack")
    void newlyEnteredLandProducesManaImmediately() {
        Permanent catacombs = harness.enterBattlefieldAndReturn(player1, new DarkwaterCatacombs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(catacombs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
