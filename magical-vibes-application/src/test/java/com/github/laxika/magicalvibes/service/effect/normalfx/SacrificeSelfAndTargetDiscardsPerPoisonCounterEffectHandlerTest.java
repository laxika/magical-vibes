package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SacrificeSelfAndTargetDiscardsPerPoisonCounterEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sacrifices source and target discards per poison counter")
            void sacrificesAndDiscardsPerPoison() {
                Card card = createCard("Flesh-Eater Imp");
                Permanent source = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(source);
                SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());
                gd.playerPoisonCounters.put(player2Id, 3);
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

                when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
            }

            @Test
            @DisplayName("No discard when target has no poison counters")
            void noDiscardWhenNoPoisonCounters() {
                Card card = createCard("Flesh-Eater Imp");
                Permanent source = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(source);
                SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

                when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no poison counters")));
            }

            @Test
            @DisplayName("Fizzles when source not found on battlefield")
            void fizzlesWhenSourceNotFound() {
                Card card = createCard("Flesh-Eater Imp");
                UUID fakePermanentId = UUID.randomUUID();
                SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, fakePermanentId);

                when(gameQueryService.findPermanentById(gd, fakePermanentId)).thenReturn(null);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("fizzles")));
            }

            @Test
            @DisplayName("Does nothing when target or source permanent is null")
            void doesNothingWhenNull() {
                Card card = createCard("Flesh-Eater Imp");
                SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            }
}
