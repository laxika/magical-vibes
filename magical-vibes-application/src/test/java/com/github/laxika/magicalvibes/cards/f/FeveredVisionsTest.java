package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeveredVisionsTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws a card at its controller's end step")
    void drawsCardAtControllerEndStep() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player1, List.of());

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals 2 damage to an opponent with four cards after drawing")
    void damagesOpponentWithFourCardsAfterDraw() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not damage its controller when they have four cards")
    void doesNotDamageController() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not damage an opponent with fewer than four cards after drawing")
    void doesNotDamageOpponentBelowFourCards() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
