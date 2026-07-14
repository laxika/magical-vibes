package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RowenTest extends BaseCardTest {

    // "Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card."

    @Test
    @DisplayName("First draw of the turn being a basic land draws an extra card")
    void firstDrawBasicLandDrawsExtra() {
        harness.addToBattlefield(player1, new Rowen());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getStackResolutionService().resolveTopOfStack(gd);

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

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

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

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
