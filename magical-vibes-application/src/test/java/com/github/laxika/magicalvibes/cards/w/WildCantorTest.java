package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildCantor.class})
class WildCantorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Wild Cantor adds one mana of the chosen color")
    void sacrificeAddsManaOfChosenColor() {
        harness.addToBattlefield(player1, new WildCantor());

        GameData gd = harness.getGameData();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Wild Cantor");
        harness.assertInGraveyard(player1, "Wild Cantor");
    }

    @Test
    @DisplayName("Wild Cantor's mana ability can be activated while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WildCantor());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Wild Cantor can add mana outside its own colors")
    void canAddManaOfAnyColor() {
        harness.addToBattlefield(player1, new WildCantor());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each color choice produces exactly one mana without using the stack")
    void eachColorResolvesImmediately(ManaColor color) {
        harness.addToBattlefield(player1, new WildCantor());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Wild Cantor");
        harness.assertInGraveyard(player1, "Wild Cantor");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped Wild Cantor can still be sacrificed for mana")
    void canActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new WildCantor()).tap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Wild Cantor");
        harness.assertInGraveyard(player1, "Wild Cantor");
    }
}
