package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermanentViewFactoryTest {

    private final CardViewFactory cardViewFactory = new CardViewFactory();
    private final PermanentViewFactory factory = new PermanentViewFactory(cardViewFactory);

    private Card card(String name, CardType type) {
        Card c = new Card();
        c.setName(name);
        c.setType(type);
        return c;
    }

    private PermanentView createWithExiled(Permanent p, List<Card> faceUpExiled, int faceDownCount) {
        return factory.create(p, 0, 0, Set.of(), false, List.of(), Set.of(), List.of(), Set.of(),
                false, false, false, Set.of(), false, Set.of(), List.of(), faceUpExiled, faceDownCount);
    }

    @Test
    @DisplayName("Face-up exiled-with cards are mapped to card views; the face-down count passes through")
    void exiledWithCardsAreMappedToViews() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));
        Card exiled = card("Gravecrawler", CardType.CREATURE);

        PermanentView view = createWithExiled(vat, List.of(exiled), 2);

        assertThat(view.exiledWithCards())
                .extracting(CardView::id, CardView::name)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(exiled.getId(), "Gravecrawler"));
        assertThat(view.faceDownExiledCount()).isEqualTo(2);
        // The factory never reveals face-down cards; the broadcast layer swaps them in per viewer.
        assertThat(view.faceDownExiledCards()).isEmpty();
    }

    @Test
    @DisplayName("Overloads without exiled-with data default to none")
    void overloadsDefaultToNoExiledCards() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));

        PermanentView view = factory.create(vat, 0, 0, Set.of(), false, List.of());

        assertThat(view.exiledWithCards()).isEmpty();
        assertThat(view.faceDownExiledCount()).isZero();
        assertThat(view.faceDownExiledCards()).isEmpty();
    }

    @Test
    @DisplayName("withFaceDownRevealed swaps the card-back count for the cards, preserving everything else")
    void withFaceDownRevealedPreservesOtherFields() {
        Permanent vat = new Permanent(card("Mimic Vat", CardType.ARTIFACT));
        vat.tap();
        Card exiled = card("Gravecrawler", CardType.CREATURE);
        PermanentView view = createWithExiled(vat, List.of(exiled), 1);
        CardView hidden = cardViewFactory.create(card("Hidden Prize", CardType.SORCERY));

        PermanentView revealed = view.withFaceDownRevealed(List.of(hidden));

        assertThat(revealed.faceDownExiledCards()).containsExactly(hidden);
        assertThat(revealed.faceDownExiledCount()).isZero();
        assertThat(revealed.id()).isEqualTo(view.id());
        assertThat(revealed.card()).isEqualTo(view.card());
        assertThat(revealed.tapped()).isTrue();
        assertThat(revealed.exiledWithCards()).isEqualTo(view.exiledWithCards());
    }
}
