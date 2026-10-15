package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(GhazbNOgre.class)
class GhazbNOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Player with strictly the most life gains control during controller's upkeep")
    void mostLifePlayerGainsControl() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.assertNotOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("Controller keeps the creature when they have the most life")
    void controllerKeepsWhenHighest() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("No control change when players are tied for the most life")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("Does not trigger when no player has strictly the most life at trigger time")
    void noTriggerWhenTieAtTriggerTime() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player2, 25);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("Does not change control when the condition fails at resolution")
    void noControlChangeWhenConditionFailsAtResolution() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }
    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("Uses the life leader at resolution rather than at trigger time")
    void lifeLeaderChangesBeforeResolution() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player2, 30);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertOnBattlefield(player2, "Ghazbán Ogre");
    }

    @Test
    @DisplayName("Triggers during the new controller's upkeep and can return control")
    void triggersForNewController() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ghazbán Ogre");

        harness.setLife(player1, 25);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghazbán Ogre");
        harness.assertNotOnBattlefield(player2, "Ghazbán Ogre");
    }
}
