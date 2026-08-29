package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubversionTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep makes each opponent lose 1 life and you gain that much")
    void ownUpkeepDrainsOpponent() {
        harness.addToBattlefield(player1, new Subversion());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new Subversion());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
