package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnterTheInfinite.class, MillennialGargoyle.class})
class EnterTheInfiniteTest extends BaseCardTest {

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new MillennialGargoyle());
        }
        return cards;
    }

    private void castEnterTheInfinite(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EnterTheInfinite()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Draws the controller's entire library and asks them to put one hand card on top")
    void drawsLibraryAndPromptsForCardToPutOnTop() {
        List<Card> library = cards(3);
        castEnterTheInfinite(library);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> {
                            assertThat(choice.playerId()).isEqualTo(player1.getId());
                            assertThat(choice.maxCount()).isEqualTo(1);
                        });

        harness.handleMultipleCardsChosen(player1, List.of(library.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(library.get(1));
    }

    @Test
    @DisplayName("The controller has no maximum hand size during the current turn's cleanup")
    void noMaximumHandSizeUntilNextTurn() {
        List<Card> library = cards(9);
        castEnterTheInfinite(library);
        harness.handleMultipleCardsChosen(player1, List.of(library.getFirst().getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("The temporary hand size exemption ends when the controller's next turn begins")
    void noMaximumHandSizeEndsOnNextTurn() {
        List<Card> library = cards(9);
        castEnterTheInfinite(library);
        harness.handleMultipleCardsChosen(player1, List.of(library.getFirst().getId()));

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);

        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).doesNotContain(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("An empty library and empty hand do not prevent the hand-size exemption")
    void emptyLibraryAndHandStillGrantNoMaximumHandSize() {
        castEnterTheInfinite(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof EnterTheInfinite);

        harness.setHand(player1, cards(8));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("With an empty library, an existing hand card must still be returned")
    void emptyLibraryStillRequiresReturningExistingHandCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card retainedCard = new MillennialGargoyle();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EnterTheInfinite(), retainedCard));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        harness.handleMultipleCardsChosen(player1, List.of(retainedCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The returned card can have been in hand before the draw")
    void canReturnCardAlreadyInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> library = cards(3);
        Card retainedCard = new MillennialGargoyle();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EnterTheInfinite(), retainedCard));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(retainedCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Putting a card back is mandatory when the hand is nonempty")
    void cannotDeclineReturningCard() {
        List<Card> library = cards(1);
        castEnterTheInfinite(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(library.getFirst().getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The opponent still discards to their normal hand limit")
    void opponentDoesNotReceiveHandSizeExemption() {
        List<Card> library = cards(9);
        castEnterTheInfinite(library);
        harness.handleMultipleCardsChosen(player1, List.of(library.getFirst().getId()));

        harness.setHand(player2, cards(8));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.DiscardChoice.class,
                        choice -> assertThat(choice.playerId()).isEqualTo(player2.getId()));
    }
}
