package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitnessTheFuture.class, DawnhartDisciple.class, Abrade.class, Island.class})
class WitnessTheFutureTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles up to four cards from the target graveyard, then looks at four and randomizes the rest")
    void shufflesTargetGraveyardThenSelectsFromOwnLibrary() {
        Card graveyardCard1 = new DawnhartDisciple();
        Card graveyardCard2 = new Abrade();
        Card graveyardCard3 = new Island();
        Card graveyardCard4 = new DawnhartDisciple();
        Card graveyardCard5 = new Abrade();
        harness.setGraveyard(player2, List.of(
                graveyardCard1, graveyardCard2, graveyardCard3, graveyardCard4, graveyardCard5));

        Card topCard1 = new DawnhartDisciple();
        Card topCard2 = new Abrade();
        Card topCard3 = new Island();
        Card topCard4 = new DawnhartDisciple();
        Card untouched = new Abrade();
        harness.setLibrary(player1, List.of(topCard1, topCard2, topCard3, topCard4, untouched));
        Card opponentLibraryCard = new Island();
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(graveyardChoice.maxCount()).isEqualTo(4);
        assertThat(graveyardChoice.validCardIds()).hasSize(5);

        harness.handleMultipleCardsChosen(player1, List.of(
                graveyardCard1.getId(), graveyardCard2.getId(), graveyardCard3.getId(),
                graveyardCard4.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice libraryChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(libraryChoice.playerId()).isEqualTo(player1.getId());
        assertThat(libraryChoice.allCards()).containsExactly(
                topCard1, topCard2, topCard3, topCard4);
        assertThat(libraryChoice.maxCount()).isEqualTo(1);
        assertThat(libraryChoice.randomRemainingToBottom()).isTrue();
        assertThat(libraryChoice.reorderRemainingToBottom()).isFalse();

        harness.handleMultipleCardsChosen(player1, List.of(topCard2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard1, topCard3, topCard4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(topCard1, topCard3, topCard4);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard5);
        assertThat(gd.playerDecks.get(player2.getId())).contains(opponentLibraryCard)
                .contains(graveyardCard1, graveyardCard2, graveyardCard3, graveyardCard4);
        harness.assertInGraveyard(player1, "Witness the Future");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no cards chosen from the target graveyard, the library effect still resolves")
    void choosingNoGraveyardCardsStillLooksAtLibrary() {
        harness.setGraveyard(player2, List.of());
        Card topCard = new DawnhartDisciple();
        harness.setLibrary(player1, List.of(topCard, new Abrade(), new Island(), new DawnhartDisciple()));
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Witness the Future");
    }
    @Test
    void canChooseZeroCardsFromNonemptyGraveyard() {
        Card graveyardCard = new DawnhartDisciple();
        Card topCard = new Island();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Witness the Future");
    }

    @Test
    void canTargetSelfAndSelectFromCardsJustShuffledIntoLibrary() {
        Card recovered = new DawnhartDisciple();
        harness.setGraveyard(player1, List.of(recovered));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(recovered.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(recovered);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(recovered);
        harness.assertInGraveyard(player1, "Witness the Future");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void looksAtAllAvailableCardsWhenLibraryHasFewerThanFour() {
        Card chosen = new Abrade();
        Card remaining = new Island();
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Witness the Future");
    }

    @Test
    void resolvesWithEmptyLibraryWithoutDrawingOrLosing() {
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Witness the Future");
    }

    @Test
    void stillSelectsFromLibraryWhenAllGraveyardCardTargetsHaveLeft() {
        Card removed = new DawnhartDisciple();
        Card selected = new Island();
        harness.setGraveyard(player2, List.of(removed));
        harness.setLibrary(player1, List.of(selected));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new WitnessTheFuture()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removed);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Witness the Future");
    }
}
