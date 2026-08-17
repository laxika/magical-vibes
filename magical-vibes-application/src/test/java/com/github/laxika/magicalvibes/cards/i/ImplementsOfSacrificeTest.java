package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementsOfSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating adds two mana of the chosen color and sacrifices itself")
    void activateAddsTwoManaAndSacrificesItself() {
        harness.addToBattlefield(player1, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Implements of Sacrifice");
        harness.assertInGraveyard(player1, "Implements of Sacrifice");
    }
}
