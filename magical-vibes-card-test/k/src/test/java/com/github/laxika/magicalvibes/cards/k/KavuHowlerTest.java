package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuHowler.class, KavuGlider.class, KavuMauler.class, Index.class})
class KavuHowlerTest extends BaseCardTest {

    private static Card createNoncreatureKavu() {
        Card card = new Card();
        card.setName("Kavu Research");
        card.setType(CardType.SORCERY);
        card.setSubtypes(List.of(CardSubtype.KAVU));
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castHowler() {
        harness.castFromHand(player1, new KavuHowler(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Kavu cards among the top four go to hand and the rest go to the bottom")
    void kavuCardsGoToHand() {
        KavuGlider kavu1 = new KavuGlider();
        Index nonKavu1 = new Index();
        KavuMauler kavu2 = new KavuMauler();
        Index nonKavu2 = new Index();
        KavuGlider deepKavu = new KavuGlider();
        harness.setLibrary(player1, List.of(kavu1, nonKavu1, kavu2, nonKavu2, deepKavu));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHowler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(kavu1, kavu2);
        assertThat(deck).containsExactly(deepKavu, nonKavu1, nonKavu2);
    }

    @Test
    @DisplayName("Noncreature Kavu cards also go to hand")
    void noncreatureKavuCardsGoToHand() {
        Card kavuSpell = createNoncreatureKavu();
        Index nonKavu = new Index();
        harness.setLibrary(player1, List.of(kavuSpell, nonKavu));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHowler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kavuSpell);
        assertThat(deck).containsExactly(nonKavu);
    }

    @Test
    @DisplayName("The controller can reverse the order of non-Kavu cards on the bottom")
    void bottomCardsCanBeReordered() {
        Index first = new Index();
        Index second = new Index();
        Index third = new Index();
        Index fourth = new Index();
        KavuGlider unrevealed = new KavuGlider();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unrevealed));

        castHowler();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, fourth, third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library containing only Kavu goes entirely to hand without reordering")
    void allKavuInShortLibraryGoToHand() {
        KavuGlider first = new KavuGlider();
        KavuMauler second = new KavuMauler();
        harness.setLibrary(player1, List.of(first, second));

        castHowler();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library produces no cards and no reorder interaction")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castHowler();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
