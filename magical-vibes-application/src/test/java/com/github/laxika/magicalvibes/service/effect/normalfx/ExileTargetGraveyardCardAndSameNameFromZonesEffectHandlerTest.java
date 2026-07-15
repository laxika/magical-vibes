package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExileTargetGraveyardCardAndSameNameFromZonesEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Does nothing when target card not found in graveyard")
            void fizzlesWhenTargetGone() {
                Card card = createCard("Surgical Extraction");
                ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();
                UUID targetCardId = UUID.randomUUID();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

                when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(null);

                resolveEffect(gd, entry, new ExileTargetGraveyardCardAndSameNameFromZonesEffect());

                verify(playerInputService, never()).beginMultiZoneExileChoice(any(), any(), any(), any(), any());
            }

            @Test
            @DisplayName("Finds matching cards across zones and begins multi-zone exile choice")
            void findsMatchingCardsAcrossZones() {
                Card card = createCard("Surgical Extraction");
                ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();

                Card targetCard = createCard("Lightning Bolt");
                UUID targetCardId = targetCard.getId();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

                Card handCopy = createCard("Lightning Bolt");
                gd.playerHands.get(player2Id).add(handCopy);
                Card graveyardCopy = createCard("Lightning Bolt");
                gd.playerGraveyards.get(player2Id).add(graveyardCopy);

                when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(targetCard);
                when(gameQueryService.findGraveyardOwnerById(gd, targetCardId)).thenReturn(player2Id);

                resolveEffect(gd, entry, new ExileTargetGraveyardCardAndSameNameFromZonesEffect());

                verify(playerInputService).beginMultiZoneExileChoice(eq(gd), eq(player1Id), any(), eq(player2Id), eq("Lightning Bolt"));
            }

            @Test
            @DisplayName("Shuffles library and logs when no matching cards found")
            void noMatchingCards() {
                Card card = createCard("Surgical Extraction");
                ExileTargetGraveyardCardAndSameNameFromZonesEffect effect = new ExileTargetGraveyardCardAndSameNameFromZonesEffect();

                Card targetCard = createCard("Unique Spell");
                UUID targetCardId = targetCard.getId();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), targetCardId);

                when(gameQueryService.findCardInGraveyardById(gd, targetCardId)).thenReturn(targetCard);
                when(gameQueryService.findGraveyardOwnerById(gd, targetCardId)).thenReturn(player2Id);
                // No matching cards anywhere

                resolveEffect(gd, entry, new ExileTargetGraveyardCardAndSameNameFromZonesEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("exiles 0 cards") && logEntry.plainText().contains("Unique Spell")));
            }
}
