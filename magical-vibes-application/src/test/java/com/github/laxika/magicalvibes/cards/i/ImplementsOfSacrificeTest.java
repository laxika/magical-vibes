package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
