package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RevealRandomCardFromTargetPlayerHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Reveals a random card from target's hand to all players")
            void revealsRandomCard() {
                Card card = createCard("Telepathy");
                RevealRandomCardFromTargetPlayerHandEffect effect = new RevealRandomCardFromTargetPlayerHandEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                Card handCard = createCard("Mountain");
                gd.playerHands.get(player2Id).add(handCard);

                CardView mockView = mock(CardView.class);
                when(cardViewFactory.create(handCard)).thenReturn(mockView);

                resolveEffect(gd, entry, new RevealRandomCardFromTargetPlayerHandEffect());

                // All players receive the reveal message
                verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
                verify(sessionManager).sendToPlayer(eq(player2Id), any(RevealHandMessage.class));
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("reveals") && msg.contains("at random")));
            }

            @Test
            @DisplayName("Does nothing when target hand is empty")
            void doesNothingWhenHandEmpty() {
                Card card = createCard("Telepathy");
                RevealRandomCardFromTargetPlayerHandEffect effect = new RevealRandomCardFromTargetPlayerHandEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new RevealRandomCardFromTargetPlayerHandEffect());

                verify(sessionManager, never()).sendToPlayer(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to reveal")));
            }
}
