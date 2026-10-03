package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VituGhaziTheCityTree;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianRiftcutter.class, BorosRecruit.class, Forest.class, VituGhaziTheCityTree.class})
class BarbarianRiftcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to destroy a target land")
    void sacrificesItselfToDestroyTargetLand() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Riftcutter");
        harness.assertInGraveyard(player1, "Barbarian Riftcutter");
        harness.assertNotOnBattlefield(player2, "Vitu-Ghazi, the City-Tree");
        harness.assertInGraveyard(player2, "Vitu-Ghazi, the City-Tree");
    }

    @Test
    @DisplayName("Can destroy a basic land")
    void canDestroyBasicLand() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the red mana cost")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barbarian Riftcutter");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before the land is destroyed")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Barbarian Riftcutter");
        harness.assertInGraveyard(player1, "Barbarian Riftcutter");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Riftcutter can destroy its controller's land")
    void canActivateWhileTappedAndSummoningSickTargetingOwnLand() {
        Permanent riftcutter = harness.addToBattlefieldAndReturn(player1, new BarbarianRiftcutter());
        riftcutter.setTapped(true);
        riftcutter.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barbarian Riftcutter");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }
}
