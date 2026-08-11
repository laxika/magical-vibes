package com.github.laxika.magicalvibes.cards.d;

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

class DrawnFromDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Keeps two of the top seven and puts the rest on the bottom randomly")
    void keepsTwoOfTopSevenAndBottomsTheRestRandomly() {
        Card[] top = {
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock(),
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock()
        };
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(top));

        harness.setHand(player1, List.of(new DrawnFromDreams()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId(), top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[0], top[1]);
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(top[7]);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrder(
                top[2], top[3], top[4], top[5], top[6]);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Puts all available cards into hand when the library has fewer than two")
    void putsAllAvailableCardsIntoHandWhenFewerThanTwo() {
        Card only = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(only);

        harness.setHand(player1, List.of(new DrawnFromDreams()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(deck).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
