package com.github.laxika.magicalvibes.cards.y;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.k.KentaroTheSmilingCat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({YomijiWhoBarsTheWay.class, KentaroTheSmilingCat.class,
        GodsEyeGateToTheReikai.class, BileUrchin.class})
class YomijiWhoBarsTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("A legendary creature the controller owns returns to their hand")
    void allyLegendaryCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Permanent kentaro = harness.addToBattlefieldAndReturn(player1, new KentaroTheSmilingCat());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kentaro));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kentaro, the Smiling Cat");
        harness.assertNotInGraveyard(player1, "Kentaro, the Smiling Cat");
    }

    @Test
    @DisplayName("An opponent's legendary permanent returns to its owner's hand, not the controller's")
    void opponentLegendaryReturnsToItsOwnersHand() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Permanent kentaro = harness.addToBattlefieldAndReturn(player2, new KentaroTheSmilingCat());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kentaro));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kentaro, the Smiling Cat");
        harness.assertNotInHand(player1, "Kentaro, the Smiling Cat");
        harness.assertNotInGraveyard(player2, "Kentaro, the Smiling Cat");
    }

    @Test
    @DisplayName("A legendary noncreature permanent also returns")
    void legendaryNoncreaturePermanentReturns() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Permanent godsEye = harness.addToBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, godsEye));
        resolveAllTriggers();

        harness.assertInHand(player1, "Gods' Eye, Gate to the Reikai");
        harness.assertNotInGraveyard(player1, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("A nonlegendary permanent does not trigger")
    void nonlegendaryPermanentStaysInGraveyard() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Permanent bileUrchin = harness.addToBattlefieldAndReturn(player1, new BileUrchin());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bileUrchin));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bile Urchin");
        harness.assertNotInHand(player1, "Bile Urchin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Yomiji does not return itself when it dies")
    void yomijiDoesNotReturnItself() {
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yomiji));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yomiji, Who Bars the Way");
        harness.assertNotInHand(player1, "Yomiji, Who Bars the Way");
    }

    @Test
    @DisplayName("A simultaneous death returns another legendary permanent but not Yomiji")
    void simultaneousDeathReturnsOnlyOtherLegendaryPermanent() {
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());
        Permanent kentaro = harness.addToBattlefieldAndReturn(player2, new KentaroTheSmilingCat());
        yomiji.setMarkedDamage(4);
        kentaro.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yomiji, Who Bars the Way");
        harness.assertNotInHand(player1, "Yomiji, Who Bars the Way");
        harness.assertInHand(player2, "Kentaro, the Smiling Cat");
        harness.assertNotInGraveyard(player2, "Kentaro, the Smiling Cat");
    }
}
