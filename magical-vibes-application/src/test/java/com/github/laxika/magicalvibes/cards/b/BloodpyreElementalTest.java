package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BloodpyreElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 2/2")
    void dealsFourDamageKillingSmallCreature() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature with toughness greater than 4 survives")
    void largeCreatureSurvives() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.addToBattlefield(player2, new AvatarOfMight());

        Permanent target = findPermanent(player2, "Avatar of Might");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avatar of Might");
    }

    @Test
    @DisplayName("Bloodpyre Elemental is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Bloodpyre Elemental");
    }
}
