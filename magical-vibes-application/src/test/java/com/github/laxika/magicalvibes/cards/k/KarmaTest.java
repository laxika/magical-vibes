package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KarmaTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Deals damage to the active player equal to the Swamps they control")
    void damagesActivePlayerBySwampCount() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Damages each player based on their own Swamps during their own upkeep")
    void damagesEachPlayerByOwnSwamps() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Only player2 is damaged (2 Swamps); player1 controls no Swamps and isn't the active player
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no Swamps")
    void noDamageWithoutSwamps() {
        harness.addToBattlefield(player1, new Karma());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }
}
