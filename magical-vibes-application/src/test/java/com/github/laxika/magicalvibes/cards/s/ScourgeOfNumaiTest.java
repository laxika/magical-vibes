package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OgreMarauder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@CardUsed({ScourgeOfNumai.class, OgreMarauder.class})
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
        harness.addToBattlefield(player1, new OgreMarauder());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An Ogre appearing before resolution stops the life loss")
    void noLifeLossWhenOgreAppearsBeforeResolution() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new OgreMarauder());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An Ogre removed before resolution does not stop the triggered life loss")
    void lifeLossWhenOgreLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreMarauder());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ogre));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An opponent's Ogre does not stop the life loss")
    void opponentOgreDoesNotHelp() {
        harness.addToBattlefield(player1, new ScourgeOfNumai());
        harness.addToBattlefield(player2, new OgreMarauder());
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
