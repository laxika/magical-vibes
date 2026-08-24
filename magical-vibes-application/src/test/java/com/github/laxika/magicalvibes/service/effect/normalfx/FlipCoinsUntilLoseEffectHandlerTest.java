package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsUntilLoseEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlipCoinsUntilLoseEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Override
    protected void setUpHandler() {
        when(coinFlipService.flip(gd, player1Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(true, 1),
                        new CoinFlipService.CoinFlipResult(false, 1));
        when(coinFlipService.replacementDetails(any())).thenReturn("");
    }

    @Test
    void stopsAtTheFirstLossAndDoesNotDispatchTheReward() {
        Card card = createCard("Squee's Revenge");
        DrawCardEffect reward = new DrawCardEffect(2);
        EffectHandler rewardHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, rewardHandler);
        FlipCoinsUntilLoseEffect effect = new FlipCoinsUntilLoseEffect(new XValue(), reward);
        StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

        resolveEffect(gd, entry, effect);

        verify(rewardHandler, never()).resolve(gd, entry, reward);
    }

    @Test
    void dispatchesTheRewardAfterAllChosenFlipsAreWon() {
        when(coinFlipService.flip(gd, player1Id))
                .thenReturn(new CoinFlipService.CoinFlipResult(true, 1));
        Card card = createCard("Squee's Revenge");
        DrawCardEffect reward = new DrawCardEffect(2);
        EffectHandler rewardHandler = mock(EffectHandler.class);
        registry.register(DrawCardEffect.class, rewardHandler);
        FlipCoinsUntilLoseEffect effect = new FlipCoinsUntilLoseEffect(new XValue(), reward);
        StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

        resolveEffect(gd, entry, effect);

        verify(rewardHandler).resolve(gd, entry, reward);
        verify(coinFlipService, times(3)).flip(gd, player1Id);
    }
}
