package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({EumidianTerrabotanist.class, Forest.class})
class EumidianTerrabotanistTest extends BaseCardTest {

    @Test
    void gainsLifeWhenYouPlayALand() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void doesNotTriggerForAnOpponentsLand() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void gainsLifeForEachLandEnteringWithoutBeingPlayed() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachCopyTriggersForItsControllersLand() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.addToBattlefield(player2, new EumidianTerrabotanist());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggerResolvesAfterTerrabotanistDies() {
        var terrabotanist = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        terrabotanist.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
