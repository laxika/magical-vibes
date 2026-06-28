package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawCardsEqualToChargeCountersOnSourceEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws cards equal to xValue (charge counters)")
            void drawsEqualToChargeCounters() {
                Card card = createCard("Shrine of Piercing Vision");
                DrawCardsEqualToChargeCountersOnSourceEffect effect = new DrawCardsEqualToChargeCountersOnSourceEffect();
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 4);

                resolveEffect(gd, entry, new DrawCardsEqualToChargeCountersOnSourceEffect());

                verify(drawService, times(4)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws 0 when no charge counters")
            void drawsZeroWhenNoChargeCounters() {
                Card card = createCard("Shrine of Piercing Vision");
                DrawCardsEqualToChargeCountersOnSourceEffect effect = new DrawCardsEqualToChargeCountersOnSourceEffect();
                StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 0);

                resolveEffect(gd, entry, new DrawCardsEqualToChargeCountersOnSourceEffect());

                verify(drawService, never()).resolveDrawCard(any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("draws 0 cards") && msg.contains("no charge counters")));
            }
}
