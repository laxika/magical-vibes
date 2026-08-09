package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Catalyst Elemental adds two red mana immediately")
    void activateAbilityAddsTwoRedManaImmediately() {
        harness.addToBattlefield(player1, new CatalystElemental());

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Catalyst Elemental");
        harness.assertInGraveyard(player1, "Catalyst Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Catalyst Elemental can be sacrificed for mana with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new CatalystElemental());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Catalyst Elemental");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }
}
