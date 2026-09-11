package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Perish.class, GrizzlyBears.class, AirElemental.class, Ornithopter.class, Regeneration.class})
class PerishTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys green creatures controlled by both players")
    void destroysGreenCreaturesFromBothPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves non-green creatures on the battlefield")
    void leavesNonGreenCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves green noncreature permanents on the battlefield")
    void leavesGreenNoncreaturePermanents() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Regeneration");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Green creatures with regeneration shields are destroyed")
    void destroysGreenCreaturesDespiteRegenerationShield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Perish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Perish(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Perish");
    }
}
