package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FaithbearerPaladin;
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

@CardUsed({PryingQuestions.class, FaithbearerPaladin.class})
class PryingQuestionsTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses 3 life and chooses a card to put on top of their library")
    void losesLifeAndPutsChosenCardOnTop() {
        Card chosenCard = new FaithbearerPaladin();
        Card remainingCard = new FaithbearerPaladin();
        Card oldTop = new FaithbearerPaladin();
        harness.setHand(player1, List.of(new PryingQuestions()));
        harness.setHand(player2, List.of(chosenCard, remainingCard));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.maxCount()).isEqualTo(1);
                });

        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
    }

    @Test
    @DisplayName("An opponent with an empty hand loses 3 life without a card choice")
    void emptyHandStillLosesLife() {
        harness.setHand(player1, List.of(new PryingQuestions()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent must put their only card onto an empty library")
    void singleCardHandAndEmptyLibrary() {
        Card chosenCard = new FaithbearerPaladin();
        harness.setHand(player1, List.of(new PryingQuestions()));
        harness.setHand(player2, List.of(chosenCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(chosenCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent cannot decline or put more than one card on top")
    void mustChooseExactlyOneCard() {
        Card firstCard = new FaithbearerPaladin();
        Card secondCard = new FaithbearerPaladin();
        harness.setHand(player1, List.of(new PryingQuestions()));
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(firstCard.getId(), secondCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player2, List.of(secondCard.getId()));

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new PryingQuestions()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
