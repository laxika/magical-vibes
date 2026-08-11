package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KithkinHarbinger;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EclipsedKithkinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Kithkin, Forest, and Plains cards among the top four")
    void etbOffersMatchingCards() {
        KithkinHarbinger kithkin = new KithkinHarbinger();
        Forest forest = new Forest();
        Plains plains = new Plains();
        setupTopCards(List.of(kithkin, new Swamp(), forest, plains));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                kithkin.getId(), forest.getId(), plains.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and bottoms the rest randomly")
    void choosingMatchingCardPutsItIntoHand() {
        Forest forest = new Forest();
        setupTopCards(List.of(forest, new Swamp(), new GrizzlyBears(), new Swamp()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all four cards")
    void decliningBottomsEverything() {
        Plains plains = new Plains();
        setupTopCards(List.of(plains, new Swamp(), new GrizzlyBears(), new Swamp()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Kithkin, Forest, or Plains among the top four, no choice is needed")
    void noMatchingCardNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Swamp(), new GrizzlyBears(), new Swamp()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedKithkin()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
