package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawXCardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws X cards based on xValue")
            void drawsXCards() {
                Card card = createCard("Stroke of Genius");
                DrawXCardsEffect effect = new DrawXCardsEffect();
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);

                resolveEffect(gd, entry, new DrawXCardsEffect());

                verify(drawService, times(5)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws nothing when X is 0")
            void drawsNothingWhenXIsZero() {
                Card card = createCard("Stroke of Genius");
                DrawXCardsEffect effect = new DrawXCardsEffect();
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

                resolveEffect(gd, entry, new DrawXCardsEffect());

                verify(drawService, never()).resolveDrawCard(any(), any());
            }
}
