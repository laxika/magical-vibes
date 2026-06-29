package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SacrificePermanentThenEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins permanent choice when matching permanents exist")
            void beginsChoiceWithMatching() {
                Card card = createCard("Goblin Bombardment");
                PermanentPredicate filter = mock(PermanentPredicate.class);
                DrawCardEffect thenEffect = new DrawCardEffect(1);
                SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent creature = new Permanent(createCard("Grizzly Bears"));
                gd.playerBattlefields.get(player1Id).add(creature);

                when(gameQueryService.matchesPermanentPredicate(gd, creature, filter)).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                        argThat(ids -> ids.contains(creature.getId())), any());
            }

            @Test
            @DisplayName("Logs and does nothing when no matching permanents")
            void noMatching() {
                Card card = createCard("Goblin Bombardment");
                PermanentPredicate filter = mock(PermanentPredicate.class);
                DrawCardEffect thenEffect = new DrawCardEffect(1);
                SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent nonMatching = new Permanent(createCard("Mountain"));
                gd.playerBattlefields.get(player1Id).add(nonMatching);

                when(gameQueryService.matchesPermanentPredicate(gd, nonMatching, filter)).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no a creature to sacrifice")));
            }
}
