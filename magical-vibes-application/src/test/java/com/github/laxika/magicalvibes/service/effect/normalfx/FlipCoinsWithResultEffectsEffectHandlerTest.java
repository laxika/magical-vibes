package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsWithResultEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class FlipCoinsWithResultEffectsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void dispatchesWinAndLossEffectsForEveryFlip() {
        Card card = createCard("Yusri, Fortune's Flame");
        CardEffect win = new DrawCardEffect(1);
        CardEffect loss = new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER);
        CardEffect allWins = new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                new AlternativeCostForSpellsEffect("{0}", null, null, false, true));
        EffectHandler winHandler = registerMockHandler(win);
        EffectHandler lossHandler = registerMockHandler(loss);
        EffectHandler allWinsHandler = registerMockHandler(allWins);
        when(coinFlipService.flipCoins(gd, player1Id, 3)).thenReturn(results(true, false, true));
        when(coinFlipService.replacementDetails(any())).thenReturn("");

        FlipCoinsWithResultEffectsEffect effect = new FlipCoinsWithResultEffectsEffect(
                new XValue(), win, loss, allWins);
        StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);
        resolveEffect(gd, entry, effect);

        verify(winHandler, times(2)).resolve(gd, entry, win);
        verify(lossHandler).resolve(gd, entry, loss);
        verify(allWinsHandler, never()).resolve(gd, entry, allWins);
    }

    @Test
    void dispatchesFiveWinRewardOnlyAfterFiveWins() {
        Card card = createCard("Yusri, Fortune's Flame");
        CardEffect win = new DrawCardEffect(1);
        CardEffect loss = new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER);
        CardEffect allWins = new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                new AlternativeCostForSpellsEffect("{0}", null, null, false, true));
        EffectHandler winHandler = registerMockHandler(win);
        EffectHandler lossHandler = registerMockHandler(loss);
        EffectHandler allWinsHandler = registerMockHandler(allWins);
        when(coinFlipService.flipCoins(gd, player1Id, 5)).thenReturn(results(true, true, true, true, true));
        when(coinFlipService.replacementDetails(any())).thenReturn("");

        FlipCoinsWithResultEffectsEffect effect = new FlipCoinsWithResultEffectsEffect(
                new XValue(), win, loss, allWins);
        StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);
        resolveEffect(gd, entry, effect);

        verify(winHandler, times(5)).resolve(gd, entry, win);
        verify(lossHandler, never()).resolve(gd, entry, loss);
        verify(allWinsHandler).resolve(gd, entry, allWins);
    }

    private EffectHandler registerMockHandler(CardEffect effect) {
        EffectHandler handler = mock(EffectHandler.class);
        registry.register(effect.getClass(), handler);
        return handler;
    }

    private List<CoinFlipService.CoinFlipResult> results(boolean... heads) {
        java.util.ArrayList<CoinFlipService.CoinFlipResult> results = new java.util.ArrayList<>();
        for (boolean headsUp : heads) {
            results.add(new CoinFlipService.CoinFlipResult(headsUp, 1));
        }
        return List.copyOf(results);
    }
}
