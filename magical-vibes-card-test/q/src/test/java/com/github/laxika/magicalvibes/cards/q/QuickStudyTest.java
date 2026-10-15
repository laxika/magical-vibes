package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuickStudy.class})
class QuickStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Quick Study draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Quick Study goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Quick Study");
    }

    @Test
    @DisplayName("Quick Study draws its controller's last two cards without causing a loss")
    void drawsLastTwoCardsWithoutLosing() {
        QuickStudy first = new QuickStudy();
        QuickStudy second = new QuickStudy();
        harness.setLibrary(player1, List.of(first, second));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("With one card remaining, Quick Study draws it and its controller loses")
    void oneCardLibraryCausesLossAfterDrawingRemainingCard() {
        QuickStudy remaining = new QuickStudy();
        harness.setLibrary(player1, List.of(remaining));

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving Quick Study with an empty library causes its controller to lose")
    void emptyLibraryCausesLoss() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
