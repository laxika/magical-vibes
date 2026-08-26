package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheStory.class, GrizzlyBears.class})
class IntoTheStoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Costs three less when an opponent has seven cards in their graveyard")
    void costsThreeLessWithSevenOpponentGraveyardCards() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not get the cost reduction with fewer than seven opponent graveyard cards")
    void doesNotGetCostReductionBelowSevenOpponentGraveyardCards() {
        harness.setGraveyard(player2, graveyardCards(6));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .<Card>mapToObj(ignored -> new GrizzlyBears())
                .toList();
    }
}
