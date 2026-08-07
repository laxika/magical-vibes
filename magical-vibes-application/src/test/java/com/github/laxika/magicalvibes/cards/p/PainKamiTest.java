package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PainKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target creature, killing a 2/2 with X=2")
    void dealsXDamageKillingSmallCreature() {
        harness.addToBattlefield(player1, new PainKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With X=1 the damage is not lethal to a 2/2")
    void smallXLeavesCreatureAlive() {
        harness.addToBattlefield(player1, new PainKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pain Kami is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new PainKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, 2, target.getId());

        harness.assertNotOnBattlefield(player1, "Pain Kami");
    }
}
