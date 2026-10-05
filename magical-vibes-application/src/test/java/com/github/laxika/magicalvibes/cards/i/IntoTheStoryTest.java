package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheStory.class, Island.class})
class IntoTheStoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

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

    @Test
    @DisplayName("Pays the full cost below the opponent graveyard threshold")
    void paysFullCostBelowThreshold() {
        harness.setGraveyard(player2, graveyardCards(6));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The caster's own graveyard does not enable the discount")
    void ownGraveyardDoesNotReduceCost() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setGraveyard(player2, graveyardCards(0));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discount does not reduce the two blue mana requirement")
    void discountPreservesBlueManaRequirement() {
        harness.setGraveyard(player2, graveyardCards(8));
        harness.setHand(player1, List.of(new IntoTheStory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still draws four after the opponent's graveyard is emptied")
    void drawsAfterGraveyardConditionStopsBeingMet() {
        harness.setGraveyard(player2, graveyardCards(8));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new IntoTheStory()));
        List<Card> library = graveyardCards(5);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.setGraveyard(player2, graveyardCards(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Into the Story");
    }

    private List<Card> graveyardCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .<Card>mapToObj(ignored -> new Island())
                .toList();
    }
}
