package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ArbiterOfKnollridge.class})
class ArbiterOfKnollridgeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sets each player's life total to the highest among all players")
    void etbSetsLifeToHighest() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 30);
        harness.castFromHand(player1, new ArbiterOfKnollridge(), "{6}{W}");

        harness.passBothPriorities(); // resolve creature spell (puts ETB on stack)
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 30);
    }

    @Test
    @DisplayName("ETB leaves life unchanged when all players already share the highest total")
    void etbNoChangeWhenAlreadyEqual() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new ArbiterOfKnollridge(), "{6}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB raises the caster when an opponent has more life")
    void etbRaisesCasterWhenOpponentAhead() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 18);
        harness.castFromHand(player1, new ArbiterOfKnollridge(), "{6}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB raises an opponent when the controller has the highest life total")
    void etbRaisesOpponentWhenControllerAhead() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        harness.castFromHand(player1, new ArbiterOfKnollridge(), "{6}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 30);
    }

    @Test
    @DisplayName("ETB uses the highest life total when its trigger resolves")
    void etbEvaluatesHighestLifeAtResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new ArbiterOfKnollridge(), "{6}{W}");

        harness.passBothPriorities();
        harness.setLife(player1, 40);
        harness.passBothPriorities();

        harness.assertLife(player1, 40);
        harness.assertLife(player2, 40);
    }
}
