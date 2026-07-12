package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoonReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's life gain is doubled")
    void controllerLifeGainDoubled() {
        harness.addToBattlefield(player1, new BoonReflection());
        harness.setLife(player1, 20);

        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);

        harness.assertLife(player1, 26); // 3 doubled to 6
    }

    @Test
    @DisplayName("Only the controller's life gain is doubled, not the opponent's")
    void opponentLifeGainNotDoubled() {
        harness.addToBattlefield(player1, new BoonReflection());
        harness.setLife(player2, 20);

        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Two Boon Reflections stack multiplicatively (quadruple)")
    void twoReflectionsQuadruple() {
        harness.addToBattlefield(player1, new BoonReflection());
        harness.addToBattlefield(player1, new BoonReflection());
        harness.setLife(player1, 20);

        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);

        harness.assertLife(player1, 32); // 3 * 4
    }

    @Test
    @DisplayName("Setting life total higher is also doubled")
    void setLifeTotalHigherDoubled() {
        harness.addToBattlefield(player1, new BoonReflection());
        harness.setLife(player1, 20);

        harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 25);

        harness.assertLife(player1, 30); // gaining 5 doubled to 10
    }
}
