package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReachThroughMists.class})
class ReachThroughMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        ReachThroughMists topCard = new ReachThroughMists();
        harness.setLibrary(player1, List.of(topCard, new ReachThroughMists()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        var opponentHandBefore = List.copyOf(gd.playerHands.get(player2.getId()));
        var opponentDeckBefore = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentDeckBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Reach Through Mists");
    }

    @Test
    @DisplayName("Drawing from an empty library loses the game")
    void drawFromEmptyDeck() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Drawing the last card does not lose the game")
    void drawingLastCardDoesNotLose() {
        ReachThroughMists lastCard = new ReachThroughMists();
        harness.setLibrary(player1, List.of(lastCard));

        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Reach Through Mists");
    }
}
