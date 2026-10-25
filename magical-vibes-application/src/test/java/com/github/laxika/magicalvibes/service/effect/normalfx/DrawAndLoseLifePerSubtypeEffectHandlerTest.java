package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawAndLoseLifePerSubtypeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws and makes losing life equal to subtype count the next step")
            void drawsAndLosesLifePerSubtype() {
                Card card = createCard("Graveborn Muse");
                DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Card zombie1Card = createCard("Zombie 1");
                zombie1Card.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
                Card zombie2Card = createCard("Zombie 2");
                zombie2Card.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
                Permanent zombie1 = new Permanent(zombie1Card);
                Permanent zombie2 = new Permanent(zombie2Card);
                gd.playerBattlefields.get(player1Id).addAll(List.of(zombie1, zombie2));

                when(gameQueryService.isCreature(gd, zombie1)).thenReturn(true);
                when(gameQueryService.isCreature(gd, zombie2)).thenReturn(true);
                when(gameQueryService.hasEffectiveSubtype(gd, zombie1, CardSubtype.ZOMBIE)).thenReturn(true);
                when(gameQueryService.hasEffectiveSubtype(gd, zombie2, CardSubtype.ZOMBIE)).thenReturn(true);
                entry.setResolvingEffectIndex(0);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
                assertThat(entry.getEffectsToResolve())
                        .containsExactly(effect, new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER));
            }

            @Test
            @DisplayName("No effect when no creatures of subtype")
            void noEffectWhenNoSubtype() {
                Card card = createCard("Graveborn Muse");
                DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("controls no") && logEntry.plainText().contains("Zombie")));
            }
}
