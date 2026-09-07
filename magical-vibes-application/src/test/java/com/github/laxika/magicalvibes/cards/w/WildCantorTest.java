package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WildCantor.class)
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
}
