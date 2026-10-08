package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingMind.class, Shock.class, Divination.class, GrizzlyBears.class, Plains.class})
class WanderingMindTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only the noncreature, nonland cards among the top six")
    void etbOffersOnlyNoncreatureNonlands() {
        Card shock = new Shock();
        Card divination = new Divination();
        setupTopCards(List.of(shock, new GrizzlyBears(), new Plains(), divination, new Plains(), new GrizzlyBears()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), divination.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing a card puts it into hand and bottoms the rest without a reorder prompt")
    void revealingPutsCardIntoHand() {
        Card shock = new Shock();
        setupTopCards(List.of(shock, new GrizzlyBears(), new Plains(), new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).doesNotContain(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining puts nothing into hand and bottoms all six")
    void decliningBottomsEverything() {
        Card shock = new Shock();
        setupTopCards(List.of(shock, new GrizzlyBears(), new Plains(), new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible card among the top six they go straight to the bottom")
    void noEligibleCardNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears(),
                new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("A short library offers every available eligible card and retains the rest")
    void shortLibraryLooksAtAvailableCards() {
        Card shock = new Shock();
        Card plains = new Plains();
        setupTopCards(List.of(plains, shock));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(plains, shock);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes the trigger without a choice or a draw")
    void emptyLibraryFinishesWithoutChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top six are offered and unexamined cards remain above the bottomed cards")
    void unexaminedCardsStayAboveBottomedCards() {
        Card shock = new Shock();
        Card bear = new GrizzlyBears();
        Card plains1 = new Plains();
        Card plains2 = new Plains();
        Card plains3 = new Plains();
        Card plains4 = new Plains();
        Card seventh = new Divination();
        Card eighth = new Plains();
        setupTopCards(List.of(shock, bear, plains1, plains2, plains3, plains4, seventh, eighth));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(shock, bear, plains1, plains2, plains3, plains4);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.subList(0, 2)).containsExactly(seventh, eighth);
        assertThat(deck.subList(2, deck.size())).containsExactlyInAnyOrder(bear, plains1, plains2, plains3, plains4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new WanderingMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
