package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeachingsOfTheArchaicsTest extends BaseCardTest {

    @Test
    @DisplayName("Does not draw when no opponent has more cards in hand")
    void doesNotDrawWhenOpponentDoesNotHaveMoreCards() {
        castTeachings(List.of(new TeachingsOfTheArchaics()), List.of(), List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws two cards when an opponent has one to three more cards")
    void drawsTwoCardsBelowFourCardDifference() {
        castTeachings(List.of(new TeachingsOfTheArchaics(), new GrizzlyBears()),
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws three cards when an opponent has at least four more cards")
    void drawsThreeCardsAtFourCardDifference() {
        castTeachings(List.of(new TeachingsOfTheArchaics(), new GrizzlyBears()),
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private void castTeachings(List<Card> controllerHand, List<Card> opponentHand, List<Card> library) {
        harness.setHand(player1, controllerHand);
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
