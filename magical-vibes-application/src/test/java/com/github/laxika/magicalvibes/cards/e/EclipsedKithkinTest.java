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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EclipsedKithkin.class, Forest.class, GrizzlyBears.class, KithkinHarbinger.class,
        Plains.class, Swamp.class})
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

    @Test
    @DisplayName("Only the top four are considered and the rest go below untouched cards")
    void onlyTopFourAreConsidered() {
        EclipsedKithkin kithkin = new EclipsedKithkin();
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        Swamp third = new Swamp();
        Plains fifth = new Plains();
        Forest sixth = new Forest();
        setupTopCards(List.of(kithkin, first, second, third, fifth, sixth));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(kithkin.getId());
        harness.handleMultipleCardsChosen(player1, List.of(kithkin.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kithkin);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(deck.subList(2, deck.size())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than four cards still allows a matching card to be chosen")
    void shortLibraryAllowsSelection() {
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        setupTopCards(List.of(plains, swamp));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(plains, swamp);
        harness.handleMultipleCardsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryResolves() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Eclipsed Kithkin");
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedKithkin()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
