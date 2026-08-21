package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a payload once for every won coin flip until the controller loses. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlipUntilLoseEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipUntilLoseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FlipUntilLoseEffect flipEffect = (FlipUntilLoseEffect) effect;
        while (true) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            boolean wonFlip = result.heads();
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameLogService.append(gameData, GameLog.text(playerName
                    + (wonFlip ? " wins" : " loses") + " the coin flip for " + entry.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));

            if (!wonFlip) {
                return;
            }

            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, entry.getControllerId());
            dispatch(gameData, entry, flipEffect.perWin());
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
            log.warn("No handler for payload effect in FlipUntilLoseEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
