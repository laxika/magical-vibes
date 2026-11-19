package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws the specified number of cards for the controller")
            void drawsSpecifiedAmount() {
                Card card = createCard("Divination");
                DrawCardEffect effect = new DrawCardEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 3);
            }

            @Test
            @DisplayName("Draws 1 card for single draw")
            void drawsSingleCard() {
                Card card = createCard("Opt");
                DrawCardEffect effect = new DrawCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 1);
            }

            @Test
            @DisplayName("Draws X cards based on the entry's xValue")
            void drawsXCards() {
                Card card = createCard("Stroke of Genius");
                DrawCardEffect effect = new DrawCardEffect(new XValue());
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 5);
            }

            @Test
            @DisplayName("Draws nothing when X is 0")
            void drawsNothingWhenXIsZero() {
                Card card = createCard("Stroke of Genius");
                DrawCardEffect effect = new DrawCardEffect(new XValue());
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 0);
            }

            @Test
            @DisplayName("CountersOnSource amount falls back to the source snapshot after the source left the battlefield")
            void countersOnSourceUsesSnapshotAfterSourceLeft() {
                Card card = createCard("Culling Dais");
                Permanent source = new Permanent(card);
                source.setCounterCount(CounterType.CHARGE, 3);
                DrawCardEffect effect = new DrawCardEffect(new CountersOnSource(CounterType.CHARGE));
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), source.getId());
                entry.setSourcePermanentSnapshot(source);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 3);
            }

            @Test
            @DisplayName("The target player draws, not the controller")
            void targetPlayerDraws() {
                Card card = createCard("Sign in Blood");
                DrawCardEffect effect = new DrawCardEffect(2, Subjects.targetPlayer());
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player2Id, 2);
                verify(drawService, never()).resolveDrawCards(eq(gd), eq(player1Id), anyInt());
            }

            @Test
            @DisplayName("The target player draws X cards from the entry's xValue")
            void targetPlayerDrawsXCards() {
                Card card = createCard("Blue Sun's Zenith");
                DrawCardEffect effect = new DrawCardEffect(new XValue(), Subjects.targetPlayer());
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 4, player2Id);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player2Id, 4);
            }

            @Test
            @DisplayName("Each opponent of the triggering player draws; the triggering player does not")
            void opponentsOfTriggeringPlayerDraw() {
                Card card = createCard("Standstill");
                DrawCardEffect effect = new DrawCardEffect(3, Subjects.opponentsOf(Subjects.triggeringPlayer()));
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id, null);

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCards(gd, player1Id, 3);
                verify(drawService, never()).resolveDrawCards(eq(gd), eq(player2Id), anyInt());
            }
}
