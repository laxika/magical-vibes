package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SneeringShadewriter.class})
class SneeringShadewriterTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldMakesEachOpponentLoseTwoLifeAndControllerGainTwoLife() {
        harness.setHand(player1, List.of(new SneeringShadewriter()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void enteringWithoutBeingCastUsesTheEnteringCreaturesController() {
        harness.setLife(player1, 13);
        harness.setLife(player2, 7);

        harness.enterBattlefieldAndReturn(player2, new SneeringShadewriter());

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 7);

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 9);
    }

    @Test
    void triggerResolvesAfterItsSourceLeavesTheBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        var source = harness.enterBattlefieldAndReturn(player1, new SneeringShadewriter());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.assertInGraveyard(player1, "Sneering Shadewriter");

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
