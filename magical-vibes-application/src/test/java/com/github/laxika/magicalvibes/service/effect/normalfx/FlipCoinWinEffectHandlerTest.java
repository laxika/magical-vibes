package com.github.laxika.magicalvibes.service.effect.normalfx;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FlipCoinWinEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("The triggering spell's caster flips the coin and determines the resolved branch")
    void triggeringSpellCasterFlipsCoin(boolean won) {
        when(coinFlipService.flip(gd, player2Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(won, 1));
        EffectHandler drawHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, drawHandler);
        DrawCardEffect winEffect = new DrawCardEffect(1);
        DrawCardEffect lossEffect = new DrawCardEffect(2);
        FlipCoinWinEffect effect = FlipCoinWinEffect.forTriggeringSpellController(winEffect, lossEffect);
        StackEntry entry = createEntry(createCard("Planar Chaos"), player1Id, List.of(effect));
        entry.setTriggeringSpell(new StackEntry(createCard("Opponent Spell"), player2Id));

        resolveEffect(gd, entry, effect);

        verify(coinFlipService).flip(gd, player2Id);
        verify(coinFlipService, never()).flip(gd, player1Id);
        verify(drawHandler).resolve(gd, entry, won ? winEffect : lossEffect);
        verify(drawHandler, never()).resolve(gd, entry, won ? lossEffect : winEffect);
        if (won) {
            assertThat(emitted(gd)).contains(new ActionEvent.CoinFlipWon(player2Id));
        } else {
            assertThat(emitted(gd)).contains(new ActionEvent.CoinFlipLost(player2Id));
        }
    }

    @Override
    protected void setUpHandler() {
        lenient().when(coinFlipService.flip(gd, player1Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(true, 1));
        when(coinFlipService.replacementDetails(any())).thenReturn("");
    }

    @Test
            @DisplayName("Always broadcasts flip result")
            void broadcastsFlipResult() {
                Card card = createCard("Krark's Thumb");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                FlipCoinWinEffect effect = new FlipCoinWinEffect(wrapped);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                // Can't control ThreadLocalRandom, but we can verify the broadcast always happens
                resolveEffect(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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

    /** The rules events emitted into {@code gameData} and not yet matched. */
    private static List<RulesEvent> emitted(GameData gameData) {
        return gameData.rulesEvents.pending().stream().flatMap(batch -> batch.events().stream()).toList();
    }
}
