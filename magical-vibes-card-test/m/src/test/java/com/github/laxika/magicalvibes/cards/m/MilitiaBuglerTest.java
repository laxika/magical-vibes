package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.h.HavocDevils;
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

@CardUsed({MilitiaBugler.class, GreenwoodSentinel.class, HavocDevils.class, Plains.class, Shock.class})
class MilitiaBuglerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only creature cards with power 2 or less among the top four")
    void etbOffersOnlySmallCreatures() {
        Card bears = new GreenwoodSentinel();
        setupTopCards(List.of(new HavocDevils(), bears, new Plains(), new Shock()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing a creature puts it into hand and bottoms the rest without a reorder prompt")
    void revealingPutsCreatureIntoHand() {
        Card bears = new GreenwoodSentinel();
        setupTopCards(List.of(bears, new HavocDevils(), new Plains(), new Shock()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining puts nothing into hand and bottoms all four")
    void decliningBottomsEverything() {
        Card bears = new GreenwoodSentinel();
        setupTopCards(List.of(bears, new HavocDevils(), new Plains(), new Shock()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no small creature among the top four they go straight to the bottom")
    void noEligibleCardNeedsNoChoice() {
        setupTopCards(List.of(new HavocDevils(), new Plains(), new Shock(), new Plains()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the top four are considered and the remainder stays above bottomed cards")
    void leavesUnlookedCardsOnTop() {
        Card large = new HavocDevils();
        Card land = new Plains();
        Card spell = new Shock();
        Card anotherLand = new Plains();
        Card fifth = new GreenwoodSentinel();
        Card sixth = new Plains();
        setupTopCards(List.of(large, land, spell, anotherLand, fifth, sixth));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(deck.subList(2, 6)).containsExactlyInAnyOrder(large, land, spell, anotherLand);
    }

    @Test
    @DisplayName("With multiple eligible creatures only the chosen one goes to hand")
    void choosesOneAmongMultipleEligibleCreatures() {
        Card first = new GreenwoodSentinel();
        Card second = new MilitiaBugler();
        Card land = new Plains();
        Card spell = new Shock();
        Card untouched = new HavocDevils();
        setupTopCards(List.of(first, second, land, spell, untouched));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 4)).containsExactlyInAnyOrder(first, land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A single eligible card in a short library can still be declined")
    void canDeclineOnlyCardInLibrary() {
        Card creature = new GreenwoodSentinel();
        setupTopCards(List.of(creature));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes the ability without a choice or a draw")
    void emptyLibraryFinishesWithoutChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new MilitiaBugler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities(); // Resolve the trigger and look at the library.
    }
}
