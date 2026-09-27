package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(PsychogenicProbe.class)
class PsychogenicProbeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who shuffles their library")
    void damagesOpponentWhoShufflesLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals 2 damage when its controller shuffles their own library")
    void triggersWhenControllerShufflesOwnLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }
}
