package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

class SneeringShadewriterTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldMakesEachOpponentLoseTwoLifeAndControllerGainTwoLife() {
        harness.setHand(player1, List.of(new SneeringShadewriter()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
