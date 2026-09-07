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

@CardUsed({GreaterMossdog.class, Forest.class, GrizzlyBears.class})
class GreaterMossdogTest extends BaseCardTest {

    @Test
    @DisplayName("May dredge Greater Mossdog instead of drawing")
    void dredgesInsteadOfDrawing() {
        GreaterMossdog mossdog = new GreaterMossdog();
        List<Card> milled = List.of(new Forest(), new GrizzlyBears(), new Forest());
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
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new Forest()));

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
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mossdog);
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
