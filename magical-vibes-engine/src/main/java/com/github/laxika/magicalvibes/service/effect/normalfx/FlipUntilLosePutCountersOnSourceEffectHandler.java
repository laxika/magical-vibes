package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLosePutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a coin-flip sequence that rewards each win with a +1/+1 counter on the source. */
@Component
@RequiredArgsConstructor
public class FlipUntilLosePutCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;
    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipUntilLosePutCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int wins = 0;
        while (true) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            boolean won = result.heads();
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameLogService.append(gameData, GameLog.text(playerName
                    + (won ? " wins" : " loses") + " the coin flip for "
                    + entry.getCard().getName() + coinFlipService.replacementDetails(result) + "."));

            if (!won) {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(
                        gameData, entry.getControllerId());
                break;
            }

            wins++;
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(
                    gameData, entry.getControllerId());
        }

        if (wins > 0) {
            putCountersOnSourceEffectHandler.resolve(gameData, entry,
                    new PutCountersOnSourceEffect(1, 1, wins));
        }
    }
}
