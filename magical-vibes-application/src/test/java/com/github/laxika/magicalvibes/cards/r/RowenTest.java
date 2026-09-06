package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rowen.class, Forest.class, GrizzlyBears.class, CityOfBrass.class})
class RowenTest extends BaseCardTest {

    // "Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card."

    @Test
    @DisplayName("First draw of the turn being a basic land draws an extra card")
    void firstDrawBasicLandDrawsExtra() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gameLogContains("reveals Forest")).isTrue();
        harness.passBothPriorities();

        // The revealed Forest plus the extra draw = two cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First draw of the turn being a nonland does not trigger")
    void firstDrawNonlandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("First draw being a nonbasic land does not trigger")
    void firstDrawNonbasicLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new CityOfBrass(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals City of Brass")).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A basic land drawn after the first draw of the turn does not trigger")
    void laterBasicLandDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new Forest()));
        // Simulate an earlier draw this turn so this is no longer the first.
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Each Rowen triggers for the same basic land draw")
    void eachRowenTriggersForTheSameBasicLandDraw() {
        harness.addToBattlefield(player1, new Rowen());
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rowen only triggers for its controller's draw")
    void onlyTriggersForItsControllersDraw() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gameLogContains("reveals Forest")).isFalse();
    }

    @Test
    @DisplayName("The first draw of a later turn triggers again")
    void firstDrawOfLaterTurnTriggersAgain() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.DRAW);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("reveals Forest")).isTrue();
    }
}
