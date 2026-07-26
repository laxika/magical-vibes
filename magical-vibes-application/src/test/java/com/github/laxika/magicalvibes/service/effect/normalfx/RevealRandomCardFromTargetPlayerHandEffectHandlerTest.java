package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
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

                resolveEffect(gd, entry, new RevealRandomCardFromTargetPlayerHandEffect());

                // All players receive the reveal message
                verify(cardRevealService).revealToAllPlayers(
                        gd, player2Id, GameEventFact.RevealZone.HAND, List.of(handCard));
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("reveals") && logEntry.plainText().contains("at random")));
            }

            @Test
            @DisplayName("Does nothing when target hand is empty")
            void doesNothingWhenHandEmpty() {
                Card card = createCard("Telepathy");
                RevealRandomCardFromTargetPlayerHandEffect effect = new RevealRandomCardFromTargetPlayerHandEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new RevealRandomCardFromTargetPlayerHandEffect());

                verifyNoInteractions(cardRevealService);
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards to reveal")));
            }
}
