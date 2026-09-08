package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsUntilLoseEffect;
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

/** Resolves a fixed-count coin-flip sequence with an all-or-nothing reward. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinsUntilLoseEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinsUntilLoseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FlipCoinsUntilLoseEffect flipEffect = (FlipCoinsUntilLoseEffect) effect;
        int coins = Math.max(0, amountEvaluationService.evaluate(gameData, flipEffect.coins(),
                AmountContext.forStackEntry(entry, null)));
        for (int i = 0; i < coins; i++) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            boolean won = result.heads();
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameLogService.append(gameData, GameLog.text(playerName
                    + (won ? " wins" : " loses") + " the coin flip for " + entry.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));
            if (!won) {
                return;
            }
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, entry.getControllerId());
        }

        dispatch(gameData, entry, flipEffect.allWinsEffect());
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
            log.warn("No handler for all-wins effect in FlipCoinsUntilLoseEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
