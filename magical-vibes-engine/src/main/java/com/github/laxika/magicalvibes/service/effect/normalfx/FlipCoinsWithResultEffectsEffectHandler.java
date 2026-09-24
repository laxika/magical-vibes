package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsWithResultEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinsWithResultEffectsEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinsWithResultEffectsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FlipCoinsWithResultEffectsEffect flipEffect = (FlipCoinsWithResultEffectsEffect) effect;
        int coins = Math.max(0, amountEvaluationService.evaluate(gameData, flipEffect.coins(),
                AmountContext.forStackEntry(entry, null)));
        var results = coinFlipService.flipCoins(gameData, entry.getControllerId(), coins);
        int wins = 0;
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        for (var result : results) {
            boolean won = result.heads();
            if (won) {
                wins++;
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, entry.getControllerId());
            } else {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, entry.getControllerId());
            }
            gameLogService.append(gameData, GameLog.text(playerName
                    + (won ? " wins" : " loses") + " the coin flip for " + entry.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));
        }

        for (var result : results) {
            dispatch(gameData, entry, result.heads() ? flipEffect.winEffect() : flipEffect.lossEffect());
        }
        if (coins == 5 && wins == 5) {
            dispatch(gameData, entry, flipEffect.allWinsEffect());
        }
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for coin-flip result effect: {}", effect.getClass().getSimpleName());
        }
    }
}
