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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(kavu1, nonKavu1, kavu2, nonKavu2, deepKavu));

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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(kavuSpell, nonKavu));

        castHowler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kavuSpell);
        assertThat(deck).containsExactly(nonKavu);
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
