package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ButcherOfMalakirTest extends BaseCardTest {

    @Test
    @DisplayName("When another creature you control dies, each opponent sacrifices a creature")
    void anotherCreatureDies() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castCruelEdictAtPlayer1();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Butcher of Malakir");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Butcher of Malakir dies, each opponent sacrifices a creature")
    void thisCreatureDies() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        harness.addToBattlefield(player2, new GiantSpider());

        castCruelEdictAtPlayer1();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Butcher of Malakir");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    private void castCruelEdictAtPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
    }
}
