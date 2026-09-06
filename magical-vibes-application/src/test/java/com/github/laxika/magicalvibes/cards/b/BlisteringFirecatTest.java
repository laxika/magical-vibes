package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(BlisteringFirecat.class)
class BlisteringFirecatTest extends BaseCardTest {

    @Test
    void sacrificesAtTheBeginningOfTheEndStepWhenCastNormally() {
        harness.setHand(player1, List.of(new BlisteringFirecat()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blistering Firecat");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blistering Firecat");
        harness.assertInGraveyard(player1, "Blistering Firecat");
    }

    @Test
    void sacrificesAtTheBeginningOfTheEndStepAfterBeingTurnedFaceUp() {
        harness.setHand(player1, List.of(new BlisteringFirecat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent firecat = findPermanent(player1, "Blistering Firecat");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firecat));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blistering Firecat");
        harness.assertInGraveyard(player1, "Blistering Firecat");
    }
}
