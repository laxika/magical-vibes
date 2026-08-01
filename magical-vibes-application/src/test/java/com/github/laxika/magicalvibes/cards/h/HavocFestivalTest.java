package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class HavocFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Players can't gain life")
    void playersCantGainLife() {
        harness.addToBattlefield(player1, new HavocFestival());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
