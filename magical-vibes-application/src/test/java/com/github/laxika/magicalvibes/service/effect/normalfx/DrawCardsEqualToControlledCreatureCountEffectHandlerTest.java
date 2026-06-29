package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawCardsEqualToControlledCreatureCountEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws cards equal to controlled creature count")
            void drawsPerCreature() {
                Card card = createCard("Collective Unconscious");
                DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent creature1 = new Permanent(createCard("Bear"));
                Permanent creature2 = new Permanent(createCard("Wolf"));
                gd.playerBattlefields.get(player1Id).addAll(List.of(creature1, creature2));

                when(gameQueryService.isCreature(gd, creature1)).thenReturn(true);
                when(gameQueryService.isCreature(gd, creature2)).thenReturn(true);

                resolveEffect(gd, entry, new DrawCardsEqualToControlledCreatureCountEffect());

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws 0 when no creatures on battlefield")
            void drawsNothingWhenNoCreatures() {
                Card card = createCard("Collective Unconscious");
                DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, new DrawCardsEqualToControlledCreatureCountEffect());

                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("draws 0 cards") && msg.contains("no creatures")));
            }

            @Test
            @DisplayName("Only counts creatures, not non-creature permanents")
            void onlyCountsCreatures() {
                Card card = createCard("Collective Unconscious");
                DrawCardsEqualToControlledCreatureCountEffect effect = new DrawCardsEqualToControlledCreatureCountEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent creature = new Permanent(createCard("Bear"));
                Permanent artifact = new Permanent(createCard("Sol Ring"));
                gd.playerBattlefields.get(player1Id).addAll(List.of(creature, artifact));

                when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
                when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);

                resolveEffect(gd, entry, new DrawCardsEqualToControlledCreatureCountEffect());

                verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
            }
}
