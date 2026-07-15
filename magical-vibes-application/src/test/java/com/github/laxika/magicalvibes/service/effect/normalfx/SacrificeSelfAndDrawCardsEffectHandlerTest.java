package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SacrificeSelfAndDrawCardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sacrifices source and draws cards")
            void sacrificesSourceAndDraws() {
                Card card = createCard("Chromatic Star");
                Permanent source = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(source);

                SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), source.getId());

                when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Does nothing when source permanent ID is null")
            void doesNothingWhenSourcePermanentIdNull() {
                Card card = createCard("Chromatic Star");
                SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(drawService, never()).resolveDrawCard(any(), any());
            }

            @Test
            @DisplayName("Fizzles when source not found on battlefield")
            void fizzlesWhenSourceNotFound() {
                Card card = createCard("Chromatic Star");
                UUID fakePermanentId = UUID.randomUUID();
                SacrificeSelfAndDrawCardsEffect effect = new SacrificeSelfAndDrawCardsEffect(2);
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), fakePermanentId);

                when(gameQueryService.findPermanentById(gd, fakePermanentId)).thenReturn(null);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("fizzles")));
            }
}
