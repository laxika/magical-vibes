package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecondSightTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent mode reorders the top five cards of the target opponent's library")
    void opponentModeReordersTargetOpponentsLibrary() {
        List<Card> topCards = cards(6);
        harness.setLibrary(player2, topCards);
        cast(new int[]{0}, List.of(player2.getId()), false);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.cards()).containsExactlyElementsOf(topCards.subList(0, 5));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(
                topCards.get(4), topCards.get(3), topCards.get(2), topCards.get(1), topCards.get(0), topCards.get(5));
    }

    @Test
    @DisplayName("Own-library mode reorders the top five cards of the controller's library")
    void ownLibraryModeReordersControllerLibrary() {
        List<Card> topCards = cards(6);
        harness.setLibrary(player1, topCards);
        cast(new int[]{1}, List.of(), false);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.deckOwnerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).containsExactlyElementsOf(topCards.subList(0, 5));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 2, 3, 4, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                topCards.get(1), topCards.get(2), topCards.get(3), topCards.get(4), topCards.get(0), topCards.get(5));
    }

    @Test
    @DisplayName("Entwine pays one additional blue mana and resolves both modes")
    void entwineResolvesBothModes() {
        List<Card> ownTopCards = cards(6);
        List<Card> opponentTopCards = cards(6);
        harness.setLibrary(player1, ownTopCards);
        harness.setLibrary(player2, opponentTopCards);
        cast(new int[]{0, 1}, List.of(player2.getId()), true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).deckOwnerId())
                .isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 2, 3, 4, 0)));

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 5))
                .containsExactly(opponentTopCards.get(4), opponentTopCards.get(3), opponentTopCards.get(2),
                        opponentTopCards.get(1), opponentTopCards.get(0));
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 5))
                .containsExactly(ownTopCards.get(1), ownTopCards.get(2), ownTopCards.get(3),
                        ownTopCards.get(4), ownTopCards.get(0));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Opponent mode rejects the controller as a target")
    void opponentModeRejectsControllerTarget() {
        harness.setHand(player1, List.of(new SecondSight()));
        addMana(false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new SecondSight()));
        addMana(entwined);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana(boolean entwined) {
        harness.addMana(player1, ManaColor.BLUE, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> new com.github.laxika.magicalvibes.cards.g.GrizzlyBears())
                .map(card -> (Card) card)
                .toList();
    }
}
