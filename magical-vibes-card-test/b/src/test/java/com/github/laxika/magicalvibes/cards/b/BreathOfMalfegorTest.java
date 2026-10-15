package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BreathOfMalfegor.class})
class BreathOfMalfegorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to each opponent, not the controller")
    void dealsFiveToEachOpponent() {
        harness.setHand(player1, List.of(new BreathOfMalfegor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent damage is relative to the spell's controller")
    void dealsDamageToOpponentWhenSecondPlayerCasts() {
        harness.setHand(player2, List.of(new BreathOfMalfegor()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }
}
