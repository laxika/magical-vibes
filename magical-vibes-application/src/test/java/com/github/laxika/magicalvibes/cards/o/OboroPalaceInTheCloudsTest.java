package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OboroPalaceInTheClouds.class})
class OboroPalaceInTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Oboro adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent oboro = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(oboro.isTapped()).isTrue();
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

    @Test
    @DisplayName("The return ability can be activated while Oboro is tapped")
    void returnAbilityCanBeActivatedWhileTapped() {
        Permanent oboro = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        oboro.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
    }

    @Test
    @DisplayName("Oboro can use its own blue mana to pay for returning itself")
    void ownManaPaysForReturn() {
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertOnBattlefield(player1, "Oboro, Palace in the Clouds");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
    }

    @Test
    @DisplayName("Multiple return activations return Oboro only once")
    void multipleReturnActivationsReturnOnlyOnce() {
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
