package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodOgre;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class ScourgeOfNumaiTest extends BaseCardTest {

    // "At the beginning of your upkeep, you lose 2 life if you don't control an Ogre."

    @Test
    @DisplayName("Without an Ogre, controller loses 2 life")
    void losesLifeWithoutOgre() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Controlling an Ogre skips the life loss")
    void noLifeLossWithOgre() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.addToBattlefield(player1, new BloodOgre());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's Ogre does not stop the life loss")
    void opponentOgreDoesNotHelp() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.addToBattlefield(player2, new BloodOgre());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
