package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Pays a coin-flip forced cost and emits the corresponding win or loss triggers. */
@Component
@RequiredArgsConstructor
public class CoinFlipCostSupport {

    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    public void pay(GameData gameData, UUID playerId, Card sourceCard, int count) {
        String playerName = gameData.playerIdToName.get(playerId);
        for (int i = 0; i < count; i++) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, playerId);
            boolean won = result.heads();
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + (won ? " wins" : " loses") + " the coin flip for ", sourceCard,
                    coinFlipService.replacementDetails(result) + "."));
            if (won) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, playerId);
            } else {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, playerId);
            }
        }
    }
}
