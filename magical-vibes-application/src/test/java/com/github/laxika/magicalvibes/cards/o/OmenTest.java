package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Omen.class)
class OmenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Omen enters library reorder state with 3 cards")
    void resolvingEntersLibraryReorderState() {
        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("After reorder, player is asked to shuffle")
    void afterReorderPlayerIsAskedToShuffle() {
        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Reordering changes which card is drawn")
    void reorderingChangesDrawnCard() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        // Put card at index 2 on top, then decline shuffle
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        harness.handleMayAbilityChosen(player1, false);

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top2);
    }

    @Test
    @DisplayName("Accepting shuffle randomizes the library before draw")
    void acceptShuffleRandomizesBeforeDraw() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Omen goes to graveyard after fully resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Omen");
    }

    @Test
    @DisplayName("A library with fewer than three cards reorders all available cards before drawing")
    void shortLibraryReordersAllAvailableCards() {
        Card first = new Omen();
        Card second = new Omen();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Accepting the shuffle logs the shuffle before drawing")
    void acceptingShuffleLogsShuffle() {
        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gameLogContains(player1.getUsername() + " shuffles their library.")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining shuffle preserves the chosen order and cards below the top three")
    void decliningShufflePreservesRemainingOrder() {
        Card first = new Omen();
        Card second = new Omen();
        Card third = new Omen();
        Card fourth = new Omen();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, fourth);
        assertThat(gameLogContains(player1.getUsername() + " shuffles their library.")).isFalse();
    }

    @Test
    @DisplayName("A one-card library can be shuffled and its last card drawn without losing")
    void oneCardLibraryCanBeShuffledAndDrawn() {
        Card lastCard = new Omen();
        harness.setLibrary(player1, List.of(lastCard));

        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Omen");
    }

    @Test
    @DisplayName("An empty library still offers the shuffle and the mandatory draw causes a loss")
    void emptyLibraryStillOffersShuffleAndDraws() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Omen(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gameLogContains(player1.getUsername() + " shuffles their library.")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
