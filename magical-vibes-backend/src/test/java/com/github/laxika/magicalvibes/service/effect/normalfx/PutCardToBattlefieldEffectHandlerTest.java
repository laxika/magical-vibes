package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PutCardToBattlefieldEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Presents card choice when matching cards exist in hand")
            void presentsChoiceWhenMatchingCards() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = mock(CardPredicate.class);
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card creatureCard = createCard("Grizzly Bears");
                gd.playerHands.get(player1Id).add(creatureCard);

                when(gameQueryService.matchesCardPredicate(eq(creatureCard), eq(predicate), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any());
            }

            @Test
            @DisplayName("Logs and does nothing when no matching cards in hand")
            void noMatchingCards() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = mock(CardPredicate.class);
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card nonMatchingCard = createCard("Mountain");
                gd.playerHands.get(player1Id).add(nonMatchingCard);

                when(gameQueryService.matchesCardPredicate(eq(nonMatchingCard), eq(predicate), any())).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no creature cards in hand")));
            }

            @Test
            @DisplayName("Does nothing when hand is empty")
            void emptyHand() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = mock(CardPredicate.class);
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no creature cards in hand")));
            }
}
