package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({GraspingLongneck.class})
class GraspingLongneckTest extends BaseCardTest {

    @Test
    void gainsTwoLifeWhenItDies() {
        harness.setLife(player1, 10);
        Permanent longneck = harness.addToBattlefieldAndReturn(player1, new GraspingLongneck());
        longneck.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    void opponentGainsLifeWhenTheirLongneckDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent longneck = harness.addToBattlefieldAndReturn(player2, new GraspingLongneck());
        longneck.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertLife(player2, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 12);
        harness.assertNotOnBattlefield(player2, "Grasping Longneck");
        harness.assertInGraveyard(player2, "Grasping Longneck");
    }

    @Test
    void eachLongneckTriggersWhenTwoDieTogether() {
        harness.setLife(player1, 10);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GraspingLongneck());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GraspingLongneck());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Grasping Longneck");
    }

    @Test
    void nonlethalDamageDoesNotGainLife() {
        harness.setLife(player1, 10);
        Permanent longneck = harness.addToBattlefieldAndReturn(player1, new GraspingLongneck());
        longneck.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player1, "Grasping Longneck");
        harness.assertNotInGraveyard(player1, "Grasping Longneck");
    }
}
