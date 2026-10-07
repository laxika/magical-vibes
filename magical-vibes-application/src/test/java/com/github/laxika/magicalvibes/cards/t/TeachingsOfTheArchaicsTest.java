package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeachingsOfTheArchaics.class, EagerFirstYear.class})
class TeachingsOfTheArchaicsTest extends BaseCardTest {

    @Test
    @DisplayName("Does not draw when no opponent has more cards in hand")
    void doesNotDrawWhenOpponentDoesNotHaveMoreCards() {
        castTeachings(List.of(new TeachingsOfTheArchaics()), List.of(), List.of(
                new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws two cards when an opponent has one to three more cards")
    void drawsTwoCardsBelowFourCardDifference() {
        castTeachings(List.of(new TeachingsOfTheArchaics(), new EagerFirstYear()),
                List.of(new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()),
                List.of(new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws three cards when an opponent has at least four more cards")
    void drawsThreeCardsAtFourCardDifference() {
        castTeachings(List.of(new TeachingsOfTheArchaics(), new EagerFirstYear()),
                List.of(new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()),
                List.of(new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8})
    @DisplayName("Draw amount uses the opponent's hand advantage before any cards are drawn")
    void drawsExactlyTheRequiredNumberOfCards(int difference) {
        castTeachings(List.of(new TeachingsOfTheArchaics(), new EagerFirstYear()),
                supportCards(1 + difference), supportCards(8));

        int expectedDraws = difference == 0 ? 0 : difference < 4 ? 2 : 3;
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1 + expectedDraws);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8 - expectedDraws);
    }

    @Test
    @DisplayName("Checks hand sizes at resolution rather than when the spell is cast")
    void checksHandSizesAtResolution() {
        harness.setHand(player1, List.of(new TeachingsOfTheArchaics(), new EagerFirstYear()));
        harness.setHand(player2, supportCards(5));
        harness.setLibrary(player1, supportCards(8));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());

        harness.setHand(player2, supportCards(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
    }

    private List<Card> supportCards(int count) {
        return IntStream.range(0, count).<Card>mapToObj(i -> new EagerFirstYear()).toList();
    }

    private void castTeachings(List<Card> controllerHand, List<Card> opponentHand, List<Card> library) {
        harness.setHand(player1, controllerHand);
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
    }
}
