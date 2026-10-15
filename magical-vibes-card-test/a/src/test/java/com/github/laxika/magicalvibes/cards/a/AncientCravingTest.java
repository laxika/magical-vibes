package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientCraving.class})
class AncientCravingTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new AncientCraving()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        cast();

        // Spell left hand, then three cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Resolving loses three life")
    void losesThreeLife() {
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Only affects its controller")
    void onlyAffectsItsController() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setLife(player2, 20);

        cast();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Drawing the last three cards does not cause a library loss")
    void drawsLastThreeCardsWithoutLosing() {
        harness.setLibrary(player1, List.of(new AncientCraving(), new AncientCraving(), new AncientCraving()));
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Ancient Craving");
    }

    @Test
    @DisplayName("A short library still causes life loss before the controller loses the game")
    void shortLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of(new AncientCraving(), new AncientCraving()));
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Life loss resolves even with fewer than three life")
    void resolvesWithLessThanThreeLife() {
        harness.setLibrary(player1, List.of(new AncientCraving(), new AncientCraving(), new AncientCraving()));
        harness.setLife(player1, 2);

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
