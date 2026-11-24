package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlipUntilLoseEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Override
    protected void setUpHandler() {
        lenient().when(coinFlipService.replacementDetails(any())).thenReturn("");
        lenient().when(coinFlipService.replacementDetailsForCoins(any())).thenReturn("");
    }

    @Nested
    @DisplayName("Flips that stop at the first loss")
    class StopAtLoss {

        @Test
        @DisplayName("Stops at the first lost flip and doesn't resolve the all-wins effect")
        void stopsAtTheFirstLossAndDoesNotDispatchTheReward() {
            when(coinFlipService.flip(gd, player1Id))
                    .thenReturn(new CoinFlipService.CoinFlipResult(true, 1),
                            new CoinFlipService.CoinFlipResult(false, 1));
            Card card = createCard("Squee's Revenge");
            DrawCardEffect reward = new DrawCardEffect(2);
            EffectHandler rewardHandler = registerMockHandler(reward);
            FlipUntilLoseEffect effect = FlipUntilLoseEffect.upTo(new XValue(), reward);
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

            resolveEffect(gd, entry, effect);

            verify(rewardHandler, never()).resolve(gd, entry, reward);
            verify(coinFlipService, times(2)).flip(gd, player1Id);
            assertThat(entry.getResolutionResults().outcome(StepKind.FLIP).amount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Resolves the all-wins effect after every chosen flip is won")
        void dispatchesTheRewardAfterAllChosenFlipsAreWon() {
            when(coinFlipService.flip(gd, player1Id))
                    .thenReturn(new CoinFlipService.CoinFlipResult(true, 1));
            Card card = createCard("Squee's Revenge");
            DrawCardEffect reward = new DrawCardEffect(2);
            EffectHandler rewardHandler = registerMockHandler(reward);
            FlipUntilLoseEffect effect = FlipUntilLoseEffect.upTo(new XValue(), reward);
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);

            resolveEffect(gd, entry, effect);

            verify(rewardHandler).resolve(gd, entry, reward);
            verify(coinFlipService, times(3)).flip(gd, player1Id);
            assertThat(entry.getResolutionResults().outcome(StepKind.FLIP).amount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Without a count, flips until a flip is lost and records the flips won")
        void flipsUntilALossWithoutACount() {
            when(coinFlipService.flip(gd, player1Id))
                    .thenReturn(new CoinFlipService.CoinFlipResult(true, 1),
                            new CoinFlipService.CoinFlipResult(true, 1),
                            new CoinFlipService.CoinFlipResult(true, 1),
                            new CoinFlipService.CoinFlipResult(false, 1));
            Card card = createCard("Crazed Firecat");
            FlipUntilLoseEffect effect = FlipUntilLoseEffect.untilLose();
            StackEntry entry = createEntry(card, player1Id, List.of(effect));

            resolveEffect(gd, entry, effect);

            verify(coinFlipService, times(4)).flip(gd, player1Id);
            assertThat(entry.getResolutionResults().outcome(StepKind.FLIP).amount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("A counted set of coins flipped at once")
    class FlippedAtOnce {

        @Test
        @DisplayName("Resolves the win and loss effects once for each flip")
        void dispatchesWinAndLossEffectsForEveryFlip() {
            Card card = createCard("Yusri, Fortune's Flame");
            CardEffect win = new DrawCardEffect(1);
            CardEffect loss = new DealDamageToPlayersEffect(2, Subjects.you());
            CardEffect allWins = GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                    new AlternativeCostForSpellsEffect("{0}", null, null, false, true));
            EffectHandler winHandler = registerMockHandler(win);
            EffectHandler lossHandler = registerMockHandler(loss);
            EffectHandler allWinsHandler = registerMockHandler(allWins);
            when(coinFlipService.flipCoins(gd, player1Id, 3)).thenReturn(results(true, false, true));

            FlipUntilLoseEffect effect = FlipUntilLoseEffect.flip(new XValue(), win, loss, allWins);
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 3);
            resolveEffect(gd, entry, effect);

            verify(winHandler, times(2)).resolve(gd, entry, win);
            verify(lossHandler).resolve(gd, entry, loss);
            verify(allWinsHandler, never()).resolve(gd, entry, allWins);
            assertThat(entry.getResolutionResults().outcome(StepKind.FLIP).amount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Resolves the all-wins effect when every flip is won")
        void dispatchesAllWinsRewardWhenEveryFlipIsWon() {
            Card card = createCard("Yusri, Fortune's Flame");
            CardEffect win = new DrawCardEffect(1);
            CardEffect loss = new DealDamageToPlayersEffect(2, Subjects.you());
            CardEffect allWins = GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                    new AlternativeCostForSpellsEffect("{0}", null, null, false, true));
            EffectHandler winHandler = registerMockHandler(win);
            EffectHandler lossHandler = registerMockHandler(loss);
            EffectHandler allWinsHandler = registerMockHandler(allWins);
            when(coinFlipService.flipCoins(gd, player1Id, 5)).thenReturn(results(true, true, true, true, true));

            FlipUntilLoseEffect effect = FlipUntilLoseEffect.flip(new XValue(), win, loss, allWins);
            StackEntry entry = createEntryWithXValue(card, player1Id, List.of(effect), 5);
            resolveEffect(gd, entry, effect);

            verify(winHandler, times(5)).resolve(gd, entry, win);
            verify(lossHandler, never()).resolve(gd, entry, loss);
            verify(allWinsHandler).resolve(gd, entry, allWins);
        }
    }

    private EffectHandler registerMockHandler(CardEffect effect) {
        EffectHandler handler = mock(EffectHandler.class);
        registry.register(effect.getClass(), handler);
        return handler;
    }

    private List<CoinFlipService.CoinFlipResult> results(boolean... heads) {
        List<CoinFlipService.CoinFlipResult> results = new ArrayList<>();
        for (boolean headsUp : heads) {
            results.add(new CoinFlipService.CoinFlipResult(headsUp, 1));
        }
        return List.copyOf(results);
    }
}
