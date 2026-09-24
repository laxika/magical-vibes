package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({SulfuricVortex.class, RenewedFaith.class})
class SulfuricVortexTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToActivePlayerOnControllerUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsTwoDamageToActivePlayerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void preventsControllerLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.castFromHand(player1, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
