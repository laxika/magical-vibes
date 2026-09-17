package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.cards.p.PlaneswalkersMirth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PhyrexianBloodstock.class, AuroraGriffin.class, MoggJailer.class, PlaneswalkersMirth.class})
class PhyrexianBloodstockTest extends BaseCardTest {

    @Test
    @DisplayName("LTB destroys the chosen white creature even with a regeneration shield")
    void leavesBattlefieldDestroysTargetWhiteCreature() {
        Permanent whiteCreature = addCreatureReady(player2, new AuroraGriffin());
        whiteCreature.setRegenerationShield(1);
        Permanent bloodstock = harness.addToBattlefieldAndReturn(player1, new PhyrexianBloodstock());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodstock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, whiteCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aurora Griffin");
        harness.assertInGraveyard(player2, "Aurora Griffin");
    }

    @Test
    @DisplayName("LTB cannot target a nonwhite creature")
    void leavesBattlefieldSkipsWhenOnlyNonwhiteCreatureAvailable() {
        addCreatureReady(player2, new MoggJailer());
        Permanent bloodstock = harness.addToBattlefieldAndReturn(player1, new PhyrexianBloodstock());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodstock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Jailer");
    }

    @Test
    @DisplayName("LTB cannot target a white noncreature permanent")
    void leavesBattlefieldSkipsWhenOnlyWhiteNoncreaturePermanentAvailable() {
        harness.addToBattlefieldAndReturn(player2, new PlaneswalkersMirth());
        Permanent bloodstock = harness.addToBattlefieldAndReturn(player1, new PhyrexianBloodstock());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bloodstock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Planeswalker's Mirth");
    }
}
