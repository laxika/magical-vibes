package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wispmare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PhyrexianBloodstockTest extends BaseCardTest {

    @Test
    @DisplayName("LTB destroys the chosen white creature without allowing regeneration")
    void leavesBattlefieldDestroysTargetWhiteCreature() {
        Permanent whiteCreature = addCreatureReady(player2, new Wispmare());
        Permanent bloodstock = harness.addToBattlefieldAndReturn(player1, new PhyrexianBloodstock());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodstock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, whiteCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wispmare");
    }

    @Test
    @DisplayName("LTB cannot target a nonwhite creature")
    void leavesBattlefieldSkipsWhenOnlyNonwhiteCreatureAvailable() {
        Permanent nonwhiteCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent bloodstock = harness.addToBattlefieldAndReturn(player1, new PhyrexianBloodstock());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodstock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
