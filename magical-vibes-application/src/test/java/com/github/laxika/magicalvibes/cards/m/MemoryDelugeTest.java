package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryDelugeTest extends BaseCardTest {

    private List<Card> setupTopCards(Card... top) {
        List<Card> cards = List.of(top);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
        return cards;
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

        harness.setHand(player1, List.of(new MemoryDeluge()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
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

        harness.setHand(player1, List.of(new MemoryDeluge()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Memory Deluge"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory Deluge"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6)
                .contains(top[7]);
    }
}
