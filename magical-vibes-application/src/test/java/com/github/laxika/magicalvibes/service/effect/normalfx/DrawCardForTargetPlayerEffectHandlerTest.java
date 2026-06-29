package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawCardForTargetPlayerEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws cards for target player")
            void drawsForTargetPlayer() {
                Card card = createCard("Temple Bell");
                DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(2, false, true);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
            }

            @Test
            @DisplayName("Does nothing when source is tapped and requireSourceUntapped is true")
            void doesNothingWhenSourceTapped() {
                Card card = createCard("Archivist");
                Permanent source = new Permanent(card);
                source.tap();

                DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

                when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

                resolveEffect(gd, entry, effect);

                verify(drawService, never()).resolveDrawCard(any(), any());
            }

            @Test
            @DisplayName("Still draws when source left battlefield (uses last known info)")
            void stillDrawsWhenSourceLeftBattlefield() {
                Card card = createCard("Archivist");
                UUID sourcePermanentId = UUID.randomUUID();

                DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, sourcePermanentId);

                when(gameQueryService.findPermanentById(gd, sourcePermanentId)).thenReturn(null);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
            }

            @Test
            @DisplayName("Draws when source is untapped and requireSourceUntapped is true")
            void drawsWhenSourceUntapped() {
                Card card = createCard("Archivist");
                Permanent source = new Permanent(card);

                DrawCardForTargetPlayerEffect effect = new DrawCardForTargetPlayerEffect(1, true, true);
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, source.getId());

                when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
            }
}
