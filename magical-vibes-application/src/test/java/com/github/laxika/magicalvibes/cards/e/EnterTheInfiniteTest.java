package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnterTheInfiniteTest extends BaseCardTest {

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
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
    @DisplayName("The controller has no maximum hand size through their next turn")
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
}
