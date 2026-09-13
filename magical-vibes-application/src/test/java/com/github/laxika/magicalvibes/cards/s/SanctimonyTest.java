package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Sanctimony.class, Mountain.class, Forest.class})
class SanctimonyTest extends BaseCardTest {

    @Test
    @DisplayName("After resolution, an opponent's Mountain tap gains the controller 1 life")
    void opponentTapsMountainGainsLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Sanctimony's life-gain trigger waits for priority and resolution")
    void lifeGainWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller tapping their own Mountain does not gain life (only opponents)")
    void controllerTapsMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Mountain is at index 1 (Sanctimony at index 0)
        harness.tapPermanent(player1, 1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent tapping a non-Mountain land does not gain life")
    void opponentTapsNonMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each opponent Mountain tap triggers Sanctimony separately")
    void multipleMountainTapsGainMultipleLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }
}
