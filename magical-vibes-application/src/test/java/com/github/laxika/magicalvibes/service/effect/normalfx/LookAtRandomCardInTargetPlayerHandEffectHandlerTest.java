package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LookAtRandomCardInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LookAtRandomCardInTargetPlayerHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Shows a random card from target's hand only to the controller")
    void showsRandomCardToControllerOnly() {
        Card card = createCard("Urza's Bauble");
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new LookAtRandomCardInTargetPlayerHandEffect()), player2Id);
        Card handCard = createCard("Mountain");
        gd.playerHands.get(player2Id).add(handCard);

        CardView mockView = mock(CardView.class);
        when(cardViewFactory.create(handCard)).thenReturn(mockView);

        resolveEffect(gd, entry, new LookAtRandomCardInTargetPlayerHandEffect());

        // Only the controller sees the card; the target does not.
        verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
        verify(sessionManager, never()).sendToPlayer(eq(player2Id), any());
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("looks at a card at random")));
    }

    @Test
    @DisplayName("Sends nothing when target hand is empty")
    void doesNothingWhenHandEmpty() {
        Card card = createCard("Urza's Bauble");
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new LookAtRandomCardInTargetPlayerHandEffect()), player2Id);

        resolveEffect(gd, entry, new LookAtRandomCardInTargetPlayerHandEffect());

        verify(sessionManager, never()).sendToPlayer(any(), any());
        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("It is empty")));
    }
}
