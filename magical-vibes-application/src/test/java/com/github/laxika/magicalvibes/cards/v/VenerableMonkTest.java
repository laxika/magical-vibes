package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VenerableMonk.class)
class VenerableMonkTest extends BaseCardTest {

    @Test
    void enteringBattlefieldPutsLifeGainTriggerOnStack() {
        harness.setLife(player1, 11);

        harness.castFromHand(player1, new VenerableMonk(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venerable Monk");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 11);
    }

    @Test
    void controllerGainsTwoLifeWhenTriggerResolves() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 13);

        harness.castFromHand(player1, new VenerableMonk(), "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 13);
    }

    @Test
    void lifeGainFollowsEnteringCreaturesController() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 13);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new VenerableMonk(), "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
    }
}
