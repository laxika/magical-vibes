package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AnHavvaTownship;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PrimalOrder.class, AnHavvaTownship.class, Forest.class})
class PrimalOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to active player equal to their nonbasic lands")
    void damagesActivePlayerByNonbasicLandCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new AnHavvaTownship());
        harness.addToBattlefield(player1, new AnHavvaTownship());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Basic lands are not counted")
    void basicLandsDoNotCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("On opponent's upkeep, damages the opponent by their own nonbasic lands")
    void damagesOpponentByTheirNonbasicLands() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player2, new AnHavvaTownship());
        harness.addToBattlefield(player1, new AnHavvaTownship());
        harness.addToBattlefield(player1, new AnHavvaTownship());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Only player2's single nonbasic land counts; controller is untouched.
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
