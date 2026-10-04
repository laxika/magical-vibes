package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeveredVisions.class, GrizzlyBears.class})
class FeveredVisionsTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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

    @Test
    @DisplayName("Checks hand size at resolution rather than when the end step begins")
    void checksHandSizeAtResolution() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player2, List.of(new FeveredVisions(), new FeveredVisions(),
                new FeveredVisions(), new FeveredVisions()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.setHand(player2, List.of(new FeveredVisions(), new FeveredVisions()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each copy checks hand size after its own draw")
    void multipleCopiesResolveSeparately() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player2, List.of(new FeveredVisions(), new FeveredVisions()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The triggered ability still draws and damages after its source leaves")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new FeveredVisions());
        harness.setHand(player2, List.of(new FeveredVisions(), new FeveredVisions(), new FeveredVisions()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
