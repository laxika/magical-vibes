package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterMossdog.class, Forest.class})
class GreaterMossdogTest extends BaseCardTest {

    @Test
    @DisplayName("May dredge Greater Mossdog instead of drawing")
    void dredgesInsteadOfDrawing() {
        GreaterMossdog mossdog = new GreaterMossdog();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(mossdog));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(mossdog);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        GreaterMossdog mossdog = new GreaterMossdog();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(mossdog));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mossdog);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot dredge when the library has fewer than three cards")
    void cannotDredgeWithTooFewLibraryCards() {
        GreaterMossdog mossdog = new GreaterMossdog();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(mossdog));
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mossdog);
    }

    @Test
    @DisplayName("Dredge mills exactly three cards and leaves the rest of the library")
    void millsExactlyThreeCards() {
        harness.setHand(player1, List.of());
        GreaterMossdog mossdog = new GreaterMossdog();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest());
        Card remaining = new Forest();
        harness.setGraveyard(player1, List.of(mossdog));
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), remaining));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mossdog);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Choosing one copy dredges only that copy")
    void returnsOnlyChosenCopy() {
        harness.setHand(player1, List.of());
        GreaterMossdog first = new GreaterMossdog();
        GreaterMossdog second = new GreaterMossdog();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, milled);

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first, milled.get(0), milled.get(1), milled.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An opponent's draw cannot dredge a card from your graveyard")
    void opponentDrawDoesNotDredge() {
        harness.setHand(player2, List.of());
        GreaterMossdog mossdog = new GreaterMossdog();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(mossdog));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(topCard, new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mossdog);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.cardsDrawnThisTurn.get(player2.getId())).isEqualTo(1);
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
