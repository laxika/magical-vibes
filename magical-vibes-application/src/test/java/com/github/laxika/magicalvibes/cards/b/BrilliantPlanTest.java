package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrilliantPlan.class)
class BrilliantPlanTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new BrilliantPlan(), "{4}{U}");
        harness.passBothPriorities();

        // Spell left hand, then three cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Drawing the last three cards does not cause a loss")
    void drawsExactlyRemainingThreeCards() {
        var first = new BrilliantPlan();
        var second = new BrilliantPlan();
        var third = new BrilliantPlan();
        harness.setLibrary(player1, List.of(first, second, third));
        var opponentHand = List.copyOf(gd.playerHands.get(player2.getId()));

        harness.castFromHand(player1, new BrilliantPlan(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Brilliant Plan");
    }

    @Test
    @DisplayName("With only two cards remaining, draws both and loses on the third draw")
    void losesWhenLibraryCannotSupplyThreeCards() {
        var first = new BrilliantPlan();
        var second = new BrilliantPlan();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new BrilliantPlan(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
