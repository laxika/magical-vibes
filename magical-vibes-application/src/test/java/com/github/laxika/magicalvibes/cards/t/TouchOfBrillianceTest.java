package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TouchOfBrilliance.class})
class TouchOfBrillianceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Touch of Brilliance draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new TouchOfBrilliance(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Touch of Brilliance goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new TouchOfBrilliance(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Touch of Brilliance");
    }

    @Test
    @DisplayName("Drawing two cards with only one card left loses to the empty library")
    void drawingPastTheEndOfTheLibraryLosesTheGame() {
        TouchOfBrilliance onlyLibraryCard = new TouchOfBrilliance();
        harness.setLibrary(player1, List.of(onlyLibraryCard));

        harness.castFromHand(player1, new TouchOfBrilliance(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
