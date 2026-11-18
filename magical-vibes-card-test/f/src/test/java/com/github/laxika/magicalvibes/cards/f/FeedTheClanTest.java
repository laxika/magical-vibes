package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.t.ThassaGodOfTheSea;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({FeedTheClan.class, AlpineGrizzly.class, ThassaGodOfTheSea.class})
class FeedTheClanTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 5 life without ferocious")
    void gainsFiveLifeWithoutFerocious() {
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Gains 10 life with ferocious")
    void gainsTenLifeWithFerocious() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
    }

    @Test
    void opponentsCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    void checksReducedPowerAtResolution() {
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        PermanentGrants.setPowerBoost(gd, creature, -1);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    void checksIncreasedPowerAtResolution() {
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        PermanentGrants.setPowerBoost(gd, creature, -1);
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        PermanentGrants.setPowerBoost(gd, creature, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
    }

    @Test
    void multipleQualifyingCreaturesStillGainOnlyTenLife() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
    }

    @Test
    void noncreatureGodDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new ThassaGodOfTheSea());
        harness.castFromHand(player1, new FeedTheClan(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }
}
