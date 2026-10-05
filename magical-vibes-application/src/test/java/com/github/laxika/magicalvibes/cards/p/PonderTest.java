package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ponder.class})
class PonderTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Ponder puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Ponder(), "{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Ponder()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — library reorder =====

    @Test
    @DisplayName("Resolving Ponder enters library reorder state with 3 cards")
    void resolvingEntersLibraryReorderState() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("Handles a library with fewer than three cards")
    void handlesLibraryWithFewerThanThreeCards() {
        Ponder first = new Ponder();
        Ponder second = new Ponder();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
    }

    // ===== Resolving — reorder then decline shuffle, then draw =====

    @Test
    @DisplayName("After reorder, player is asked to shuffle")
    void afterReorderPlayerIsAskedToShuffle() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        // Complete reorder (keep same order)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining shuffle and drawing keeps top card")
    void declineShuffleDrawsTopCard() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        // Keep same order
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        // Should have drawn the top card
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top0);
    }

    @Test
    @DisplayName("Reordering changes which card is drawn")
    void reorderingChangesDrawnCard() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        // Put card at index 2 on top
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        // Should have drawn what was originally at index 2
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.get(0)).isSameAs(top2);
    }

    // ===== Resolving — reorder then accept shuffle, then draw =====

    @Test
    @DisplayName("Accepting shuffle randomizes the library before draw")
    void acceptShuffleRandomizesBeforeDraw() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        // Complete reorder
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        // Accept shuffle
        harness.handleMayAbilityChosen(player1, true);

        // Should still have drawn 1 card
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        // Deck should have lost 1 card from draw
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Goes to graveyard =====

    @Test
    @DisplayName("Ponder goes to graveyard after fully resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        // Complete reorder
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        // Decline shuffle
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ponder");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Accepting shuffle logs shuffle message")
    void acceptingShuffleLogsMessage() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Declining shuffle logs decline message")
    void decliningShuffleLogsMessage() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses not to shuffle"));
    }

    @Test
    @DisplayName("Reordering preserves the other top cards and the rest of the library")
    void preservesRemainingLibraryOrder() {
        Ponder first = new Ponder();
        Ponder second = new Ponder();
        Ponder third = new Ponder();
        Ponder fourth = new Ponder();
        Ponder fifth = new Ponder();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first, fourth, fifth);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, fourth, fifth);
    }

    @Test
    @DisplayName("A one-card library can be shuffled and its only card is drawn")
    void drawsOnlyCardAfterShuffle() {
        Ponder onlyCard = new Ponder();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Ponder");
    }

    @Test
    @DisplayName("An empty library still permits shuffling before the failed draw")
    void emptyLibraryStillOffersShuffleThenLosesOnDraw() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the caster may reorder the cards")
    void opponentCannotReorderCards() {
        harness.castFromHand(player1, new Ponder(), "{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
