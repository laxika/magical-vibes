package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Revitalize.class})
class RevitalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Revitalize gains 3 life and draws a card")
    void resolvingGainsLifeAndDraws() {
        harness.setLife(player1, 17);
        harness.setLibrary(player1, List.of(new Revitalize()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Revitalize");
    }

    @Test
    @DisplayName("Revitalize does not affect the opponent's life or hand")
    void doesNotAffectOpponent() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 17);
        harness.setLibrary(player1, List.of(new Revitalize()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Drawing the last card gains life without causing a loss")
    void drawingLastCardDoesNotCauseLoss() {
        Revitalize drawnCard = new Revitalize();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Revitalize");
    }

    @Test
    @DisplayName("An empty library still allows life gain before the controller loses")
    void emptyLibraryGainsLifeThenLoses() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Revitalize()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
