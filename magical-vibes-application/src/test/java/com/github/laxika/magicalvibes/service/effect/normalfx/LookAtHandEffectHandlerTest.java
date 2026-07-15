package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LookAtHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Reveals hand contents to controller")
            void revealsHandToController() {
                Card card = createCard("Telepathy");
                LookAtHandEffect effect = new LookAtHandEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                Card handCard = createCard("Mountain");
                gd.playerHands.get(player2Id).add(handCard);

                CardView mockView = mock(CardView.class);
                when(cardViewFactory.create(handCard)).thenReturn(mockView);

                resolveEffect(gd, entry, new LookAtHandEffect());

                verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealHandMessage.class));
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("looks at") && logEntry.plainText().contains("Player2") && logEntry.plainText().contains("Mountain")));
            }

            @Test
            @DisplayName("Handles empty hand")
            void handlesEmptyHand() {
                Card card = createCard("Telepathy");
                LookAtHandEffect effect = new LookAtHandEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new LookAtHandEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("looks at") && logEntry.plainText().contains("empty")));
            }
}
