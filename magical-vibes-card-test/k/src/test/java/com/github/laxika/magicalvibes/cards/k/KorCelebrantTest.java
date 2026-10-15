package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({KorCelebrant.class, CliffhavenSellSword.class})
class KorCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when it enters")
    void gainsLifeWhenItEnters() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KorCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeWhenAllyCreatureEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.setHand(player1, List.of(new CliffhavenSellSword()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void doesNotGainLifeWhenOpponentsCreatureEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CliffhavenSellSword()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Both Celebrants trigger when a second Celebrant enters")
    void bothCelebrantsTriggerWhenSecondEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.setHand(player1, List.of(new KorCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Celebrant gains life for its controller")
    void opponentControlledCelebrantGainsLifeForOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new KorCelebrant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CliffhavenSellSword()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }
}
