package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
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

                verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws 1 card for single draw")
            void drawsSingleCard() {
                Card card = createCard("Opt");
                DrawCardEffect effect = new DrawCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws X cards based on the entry's xValue")
            void drawsXCards() {
                Card card = createCard("Stroke of Genius");
                DrawCardEffect effect = new DrawCardEffect(new XValue());
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);

                resolveEffect(gd, entry, effect);

                verify(drawService, times(5)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws nothing when X is 0")
            void drawsNothingWhenXIsZero() {
                Card card = createCard("Stroke of Genius");
                DrawCardEffect effect = new DrawCardEffect(new XValue());
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

                resolveEffect(gd, entry, effect);

                verify(drawService, never()).resolveDrawCard(any(), any());
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

                verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
            }
}
