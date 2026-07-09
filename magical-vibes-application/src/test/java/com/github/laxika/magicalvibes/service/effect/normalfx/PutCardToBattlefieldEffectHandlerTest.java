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
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

class PutCardToBattlefieldEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Presents card choice when matching cards exist in hand")
            void presentsChoiceWhenMatchingCards() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card creatureCard = createCard("Grizzly Bears");
                gd.playerHands.get(player1Id).add(creatureCard);

                when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
            }

            @Test
            @DisplayName("Passes haste and end-step-sacrifice flags through to the card choice")
            void passesHasteAndSacrificeFlags() {
                Card card = createCard("Incandescent Soulstoke");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                PutCardToBattlefieldEffect effect =
                        new PutCardToBattlefieldEffect(predicate, "Elemental creature", false, false, true, true);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card creatureCard = createCard("Air Elemental");
                gd.playerHands.get(player1Id).add(creatureCard);

                when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), eq(false), eq(true), eq(true));
            }

            @Test
            @DisplayName("Logs and does nothing when no matching cards in hand")
            void noMatchingCards() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card nonMatchingCard = createCard("Mountain");
                gd.playerHands.get(player1Id).add(nonMatchingCard);

                when(predicateEvaluationService.matchesCardPredicate(eq(nonMatchingCard), eq(predicate), any())).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no creature cards in hand")));
            }

            @Test
            @DisplayName("Does nothing when hand is empty")
            void emptyHand() {
                Card card = createCard("Elvish Piper");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginCardChoice(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no creature cards in hand")));
            }
}
