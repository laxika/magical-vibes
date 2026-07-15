package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FlipCoinWinEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Always broadcasts flip result")
            void broadcastsFlipResult() {
                Card card = createCard("Krark's Thumb");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                FlipCoinWinEffect effect = new FlipCoinWinEffect(wrapped);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                // Can't control ThreadLocalRandom, but we can verify the broadcast always happens
                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("coin flip") && logEntry.plainText().contains("Player1")));
            }
}
