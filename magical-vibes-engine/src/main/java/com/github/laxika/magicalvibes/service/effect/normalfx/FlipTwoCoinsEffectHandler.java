package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipTwoCoinsEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipTwoCoinsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlipTwoCoinsEffect) effect;

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<CoinFlipService.CoinFlipResult> results = coinFlipService.flipCoins(gameData, controllerId, 2);
        CoinFlipService.CoinFlipResult firstResultValue = results.get(0);
        CoinFlipService.CoinFlipResult secondResultValue = results.get(1);
        boolean firstFlip = firstResultValue.heads();
        boolean secondFlip = secondResultValue.heads();

        String firstResult = (firstFlip ? "heads" : "tails") + coinFlipService.replacementDetails(firstResultValue);
        String secondResult = (secondFlip ? "heads" : "tails") + coinFlipService.replacementDetails(secondResultValue);
        if (firstFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        }
        if (secondFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        }

        gameLogService.append(gameData, GameLog.text(playerName + " flips two coins for " + sourceName + ": " + firstResult + " and " + secondResult + "."));

        CardEffect chosen;
        if (firstFlip && secondFlip) {
            chosen = e.bothHeads();
        } else if (!firstFlip && !secondFlip) {
            chosen = e.bothTails();
        } else {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(chosen);
        if (handler != null) {
            handler.resolve(gameData, entry, chosen);
        } else {
            log.warn("No handler for wrapped effect in FlipTwoCoinsEffect: {}",
                    chosen.getClass().getSimpleName());
        }
    
    }
}
