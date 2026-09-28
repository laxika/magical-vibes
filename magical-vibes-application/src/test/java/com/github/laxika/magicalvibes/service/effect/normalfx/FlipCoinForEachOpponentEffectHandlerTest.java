package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachOpponentEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlipCoinForEachOpponentEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void dispatchesWinBranchForEachOpponent() {
        DrawCardEffect win = new DrawCardEffect();
        DealDamageToPlayersEffect loss = new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER);
        EffectHandler winHandler = mock(EffectHandler.class);
        EffectHandler lossHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, winHandler);
        registry.register(DealDamageToPlayersEffect.class, lossHandler);
        when(coinFlipService.flip(gd, player1Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(true, 1));

        FlipCoinForEachOpponentEffect effect = new FlipCoinForEachOpponentEffect(win, loss);
        StackEntry entry = createTriggeredEntry(createCard("Mutalith Vortex Beast"), player1Id,
                List.of(effect), null);
        resolveEffect(gd, entry, effect);

        verify(winHandler).resolve(gd, entry, win);
        verify(lossHandler, never()).resolve(gd, entry, loss);
        verify(triggerCollectionService).checkControllerWinsCoinFlipTriggers(gd, player1Id);
    }

    @Test
    void bindsTheOpponentToTheLossBranch() {
        DrawCardEffect win = new DrawCardEffect();
        DealDamageToPlayersEffect loss = new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER);
        EffectHandler winHandler = mock(EffectHandler.class);
        EffectHandler lossHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, winHandler);
        registry.register(DealDamageToPlayersEffect.class, lossHandler);
        when(coinFlipService.flip(gd, player1Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(false, 1));
        doAnswer(invocation -> {
            assertThat(entryTarget(invocation.getArgument(1))).isEqualTo(player2Id);
            return null;
        }).when(lossHandler).resolve(any(), any(), any());

        FlipCoinForEachOpponentEffect effect = new FlipCoinForEachOpponentEffect(win, loss);
        StackEntry entry = createTriggeredEntry(createCard("Mutalith Vortex Beast"), player1Id,
                List.of(effect), null);
        resolveEffect(gd, entry, effect);

        verify(lossHandler).resolve(gd, entry, loss);
        verify(winHandler, never()).resolve(gd, entry, win);
        verify(triggerCollectionService).checkControllerLosesCoinFlipTriggers(gd, player1Id);
        assertThat(entry.getTargetId()).isNull();
    }

    private java.util.UUID entryTarget(Object argument) {
        return ((StackEntry) argument).getTargetId();
    }
}
