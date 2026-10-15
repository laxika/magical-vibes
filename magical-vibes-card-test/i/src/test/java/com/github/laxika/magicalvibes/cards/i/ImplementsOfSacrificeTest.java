package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed(ImplementsOfSacrifice.class)
class ImplementsOfSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating adds two mana of the chosen color and sacrifices itself")
    void activateAddsTwoManaAndSacrificesItself() {
        harness.addToBattlefield(player1, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Implements of Sacrifice");
        harness.assertInGraveyard(player1, "Implements of Sacrifice");
    }

    @Test
    @DisplayName("Cannot activate without paying the generic activation cost")
    void cannotActivateWithoutGenericMana() {
        Permanent implementsOfSacrifice = harness.addToBattlefieldAndReturn(player1, new ImplementsOfSacrifice());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(implementsOfSacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(implementsOfSacrifice.getCard());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("One color choice produces both mana immediately for the activating player")
    void producesTwoManaOfEachChosenColor(ManaColor color) {
        harness.addToBattlefield(player1, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Implements of Sacrifice");
        harness.assertInGraveyard(player1, "Implements of Sacrifice");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped source cannot activate and does not spend mana or sacrifice itself")
    void cannotActivateWhenTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ImplementsOfSacrifice());
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Implements of Sacrifice");
        harness.assertNotInGraveyard(player1, "Implements of Sacrifice");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly cast artifact can activate using colored mana to pay its generic cost")
    void canActivateImmediatelyAfterEntering() {
        harness.castFromHand(player1, new ImplementsOfSacrifice(), "{2}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Implements of Sacrifice");
        harness.assertInGraveyard(player1, "Implements of Sacrifice");
    }
}
