package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HavocFestival.class, AngelOfMercy.class})
class HavocFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Players can't gain life")
    void playersCantGainLife() {
        harness.addToBattlefield(player1, new HavocFestival());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Controller loses half life (rounded up) on own upkeep")
    void controllerLosesHalfLifeOnOwnUpkeep() {
        harness.addToBattlefield(player1, new HavocFestival());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent loses half life (rounded up) on their upkeep")
    void opponentLosesHalfLifeOnOwnUpkeep() {
        harness.addToBattlefield(player1, new HavocFestival());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Odd life total rounds the loss up")
    void oddLifeRoundsUp() {
        harness.addToBattlefield(player1, new HavocFestival());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
    }

    @Test
    void opponentCantGainLife() {
        harness.addToBattlefield(player1, new HavocFestival());

        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    void pendingTriggerUsesLifeTotalAtResolution() {
        harness.addToBattlefield(player1, new HavocFestival());
        advanceToUpkeep(player1);
        harness.assertLife(player1, 20);

        harness.setLife(player1, 13);
        harness.passBothPriorities();

        harness.assertLife(player1, 6);
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleFestivalsHalveLifeSequentially() {
        harness.addToBattlefield(player1, new HavocFestival());
        harness.addToBattlefield(player2, new HavocFestival());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, 9);
        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        harness.assertLife(player2, 20);
    }

    @Test
    void oneLifeIsLostRatherThanRoundedToZero() {
        harness.addToBattlefield(player1, new HavocFestival());
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 20);
    }
}
