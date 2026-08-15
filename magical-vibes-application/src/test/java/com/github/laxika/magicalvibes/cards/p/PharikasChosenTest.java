package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PharikasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a creature it damages in combat")
    void deathtouchDestroysCreatureItDamagesInCombat() {
        harness.addToBattlefield(player1, new PharikasChosen());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent chosen = gd.playerBattlefields.get(player1.getId()).getFirst();
        chosen.setSummoningSick(false);
        chosen.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
