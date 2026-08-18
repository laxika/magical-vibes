package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OboroPalaceInTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Oboro adds one blue mana")
    void tappingAddsBlueMana() {
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying one mana returns Oboro to its owner's hand")
    void payingOneManaReturnsOboroToHand() {
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
