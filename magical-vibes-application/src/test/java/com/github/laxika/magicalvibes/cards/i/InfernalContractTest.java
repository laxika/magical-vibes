package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InfernalContract.class)
class InfernalContractTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards and loses half life rounded up (even life total)")
    void drawsFourAndLosesHalfEven() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(
                new InfernalContract(), new InfernalContract(), new InfernalContract(), new InfernalContract()));

        harness.castFromHand(player1, new InfernalContract(), "{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Infernal Contract");
    }

    @Test
    @DisplayName("Odd life total rounds the loss up")
    void oddLifeRoundsUp() {
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(
                new InfernalContract(), new InfernalContract(), new InfernalContract(), new InfernalContract()));

        harness.castFromHand(player1, new InfernalContract(), "{B}{B}{B}");
        harness.passBothPriorities();

        // Half of 15 rounded up is 8, leaving 7.
        harness.assertLife(player1, 7);
    }

    @Test
    @DisplayName("One life is rounded up to a one-life loss")
    void oneLifeRoundsUpToOneLifeLoss() {
        harness.setLife(player1, 1);
        harness.setLibrary(player1, List.of(
                new InfernalContract(), new InfernalContract(), new InfernalContract(), new InfernalContract()));

        harness.castFromHand(player1, new InfernalContract(), "{B}{B}{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
    }
}
