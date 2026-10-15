package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryDeluge.class, GrizzlyBears.class, Shock.class})
class MemoryDelugeTest extends BaseCardTest {

    private void setupTopCards(Card... top) {
        harness.setLibrary(player1, List.of(top));
    }

    @Test
    @DisplayName("Looks at mana spent (4), keeps two, rest on bottom randomly (no reorder)")
    void normalCastLooksAtFourKeepsTwo() {
        Card c0 = new GrizzlyBears();
        Card c1 = new Shock();
        Card c2 = new GrizzlyBears();
        Card c3 = new Shock();
        Card untouched = new GrizzlyBears();
        setupTopCards(c0, c1, c2, c3, untouched);

        harness.castFromHand(player1, new MemoryDeluge(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.reorderRemainingToBottom()).isFalse();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.allCards()).containsExactly(c0, c1, c2, c3);

        harness.handleMultipleCardsChosen(player1, List.of(c0.getId(), c1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(c0, c1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(3);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 3)).containsExactlyInAnyOrder(c2, c3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Fewer than two looked-at cards: all go to hand")
    void fewerThanTwoGoToHand() {
        Card only = new GrizzlyBears();
        setupTopCards(only);

        harness.castFromHand(player1, new MemoryDeluge(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback looks at seven, keeps two, exiles the spell")
    void flashbackLooksAtSevenAndExiles() {
        Card[] top = new Card[8];
        for (int i = 0; i < 8; i++) {
            top[i] = new GrizzlyBears();
        }
        setupTopCards(top);

        harness.setGraveyard(player1, List.of(new MemoryDeluge()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(7);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId(), top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[0], top[1]);
        harness.assertNotInGraveyard(player1, "Memory Deluge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory Deluge"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6)
                .contains(top[7]);
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new MemoryDeluge(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Memory Deluge");
    }

    @Test
    @DisplayName("Exactly two available library cards both go to hand")
    void exactlyTwoCardsGoToHandWithoutChoice() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        setupTopCards(first, second);
        harness.castFromHand(player1, new MemoryDeluge(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing fewer than two is rejected when enough cards are available")
    void mustChooseTwoCardsWhenAvailable() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        setupTopCards(first, second, third);
        harness.castFromHand(player1, new MemoryDeluge(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
