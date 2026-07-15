package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Discards all cards from hand to graveyard")
    void discardsAllCards() {
        Card card = createCard("One with Nothing");
        Card handCard1 = createCard("Mountain");
        Card handCard2 = createCard("Forest");
        gd.playerHands.get(player1Id).addAll(List.of(handCard1, handCard2));
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard1);
        verify(graveyardService).addCardToGraveyard(gd, player1Id, handCard2);
        verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard1);
        verify(triggerCollectionService).checkDiscardTriggers(gd, player1Id, handCard2);
    }

    @Test
    @DisplayName("Does nothing with empty hand")
    void doesNothingWithEmptyHand() {
        Card card = createCard("One with Nothing");
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no cards to discard")));
    }

    @Test
    @DisplayName("Sets discardCausedByOpponent to false")
    void setsDiscardCausedByOpponent() {
        Card card = createCard("One with Nothing");
        gd.playerHands.get(player1Id).add(createCard("Mountain"));
        gd.discardCausedByOpponent = true;
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        assertThat(gd.discardCausedByOpponent).isFalse();
    }

    @Test
    @DisplayName("Logs discard count correctly")
    void logsDiscardCount() {
        Card card = createCard("One with Nothing");
        gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));
        StackEntry entry = createEntry(card, player1Id, List.of(new DiscardHandEffect()));

        resolveEffect(gd, entry, new DiscardHandEffect());

        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("discards their hand") && logEntry.plainText().contains("3 cards")));
    }
}
