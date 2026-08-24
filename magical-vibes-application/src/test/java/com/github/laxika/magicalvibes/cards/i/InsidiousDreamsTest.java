package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsidiousDreams.class, GiantGrowth.class, GrizzlyBears.class})
class InsidiousDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and puts X searched cards on top in the chosen order")
    void discardsAndSearchesForXCards() {
        Card topCandidate = new GiantGrowth();
        Card secondCandidate = new GrizzlyBears();
        Card remainingCandidate = new GiantGrowth();
        setLibrary(List.of(topCandidate, secondCandidate, remainingCandidate));
        harness.setHand(player1, List.of(new InsidiousDreams(), new GiantGrowth(),
                new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(topCandidate.getId(), secondCandidate.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondCandidate, topCandidate, remainingCandidate);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Insidious Dreams", "Giant Growth", "Grizzly Bears");
    }

    @Test
    @DisplayName("The search requires exactly X cards when the library contains enough cards")
    void searchRequiresExactlyXCards() {
        Card firstCandidate = new GiantGrowth();
        Card secondCandidate = new GrizzlyBears();
        setLibrary(List.of(firstCandidate, secondCandidate, new GiantGrowth()));
        harness.setHand(player1, List.of(new InsidiousDreams(), new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstCandidate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 2 cards");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(firstCandidate.getId(), secondCandidate.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X=0 discards no cards and does not prompt for a search")
    void xZeroDoesNotDiscardOrPrompt() {
        Card libraryCard = new GiantGrowth();
        Card otherLibraryCard = new GrizzlyBears();
        setLibrary(List.of(libraryCard, otherLibraryCard));
        harness.setHand(player1, List.of(new InsidiousDreams(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, otherLibraryCard);
    }

    private void setLibrary(List<Card> cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(cards);
    }
}
