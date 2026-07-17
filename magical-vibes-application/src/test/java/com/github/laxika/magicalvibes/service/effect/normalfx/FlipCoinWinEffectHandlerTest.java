package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

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

    @Test
    @DisplayName("A SequenceEffect branch resolves each step in order against the same entry")
    void sequenceBranchResolvesEachStep() {
        Card card = createCard("Krark's Thumb");
        EffectHandler drawHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, drawHandler);

        DrawCardEffect step1 = new DrawCardEffect(1);
        DrawCardEffect step2 = new DrawCardEffect(2);
        SequenceEffect sequence = SequenceEffect.of(step1, step2);
        // Same branch on win and loss so the (uncontrollable) coin-flip outcome doesn't matter:
        // either way the SequenceEffect must be expanded into its steps and each dispatched.
        FlipCoinWinEffect effect = new FlipCoinWinEffect(sequence, sequence);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(drawHandler).resolve(gd, entry, step1);
        verify(drawHandler).resolve(gd, entry, step2);
    }
}
