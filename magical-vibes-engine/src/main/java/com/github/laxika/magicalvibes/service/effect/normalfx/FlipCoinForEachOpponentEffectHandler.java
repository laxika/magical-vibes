package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves one coin flip and result branch for each opponent. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinForEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinForEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        FlipCoinForEachOpponentEffect flipEffect = (FlipCoinForEachOpponentEffect) effect;
        UUID controllerId = entry.getControllerId();

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }

            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
            boolean won = result.heads();
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName
                    + (won ? " wins" : " loses") + " the coin flip for " + entry.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));

            if (won) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
                dispatch(gameData, entry, flipEffect.winEffect());
            } else {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, controllerId);
                UUID previousTargetId = entry.getTargetId();
                entry.setTargetIdForEffectResolution(opponentId);
                try {
                    dispatch(gameData, entry, flipEffect.lossEffect());
                } finally {
                    entry.restoreTargetIdAfterEffectResolution(previousTargetId);
                }
            }
        }
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect == null) {
            return;
        }
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
