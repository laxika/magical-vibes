package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LookAtRandomCardInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
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

        resolveEffect(gd, entry, new LookAtRandomCardInTargetPlayerHandEffect());

        // Only the controller sees the card; the target does not.
        verify(cardRevealService).revealToPlayer(
                gd, player2Id, GameEventFact.RevealZone.HAND, List.of(handCard), player1Id);
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("looks at a card at random")));
    }

    @Test
    @DisplayName("Sends nothing when target hand is empty")
    void doesNothingWhenHandEmpty() {
        Card card = createCard("Urza's Bauble");
        StackEntry entry = createEntryWithTarget(card, player1Id,
                List.of(new LookAtRandomCardInTargetPlayerHandEffect()), player2Id);

        resolveEffect(gd, entry, new LookAtRandomCardInTargetPlayerHandEffect());

        verifyNoInteractions(cardRevealService);
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("It is empty")));
    }
}
