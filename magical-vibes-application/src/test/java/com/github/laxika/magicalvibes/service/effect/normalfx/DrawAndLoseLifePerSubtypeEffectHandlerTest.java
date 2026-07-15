package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
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
            @DisplayName("Draws and loses life equal to subtype count")
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

                when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(18);
            }

            @Test
            @DisplayName("No effect when no creatures of subtype")
            void noEffectWhenNoSubtype() {
                Card card = createCard("Graveborn Muse");
                DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("controls no") && logEntry.plainText().contains("Zombie")));
            }

            @Test
            @DisplayName("Draws but does not lose life when life can't change")
            void drawsButNoLifeLossWhenCantChange() {
                Card card = createCard("Graveborn Muse");
                DrawAndLoseLifePerSubtypeEffect effect = new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Card zombieCard = createCard("Zombie");
                zombieCard.setSubtypes(new ArrayList<>(List.of(CardSubtype.ZOMBIE)));
                Permanent zombie = new Permanent(zombieCard);
                gd.playerBattlefields.get(player1Id).add(zombie);

                when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("life total can't change")));
            }
}
