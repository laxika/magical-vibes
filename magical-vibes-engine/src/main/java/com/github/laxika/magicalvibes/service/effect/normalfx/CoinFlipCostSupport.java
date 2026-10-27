package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Pays a coin-flip forced cost and emits the corresponding win or loss triggers.
 *
 * <p>Every coin is flipped before any result is logged or triggers, so when the payment runs inside
 * {@link CoinFlipService#answerChoosingResultsFirst} a Krark's Thumb choice on any of its flips can still stop
 * it before anything else changed.</p>
 */
@Component
@RequiredArgsConstructor
public class CoinFlipCostSupport {

    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    public void pay(GameData gameData, UUID playerId, Card sourceCard, int count) {
        String playerName = gameData.playerIdToName.get(playerId);
        List<CoinFlipService.CoinFlipResult> results =
                coinFlipService.flipEach(gameData, Collections.nCopies(count, playerId));
        for (CoinFlipService.CoinFlipResult result : results) {
            boolean won = result.heads();
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + (won ? " wins" : " loses") + " the coin flip for ", sourceCard,
                    coinFlipService.replacementDetails(result) + "."));
            if (won && result.isActualCoinFlip()) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, playerId);
            } else if (result.isActualCoinFlip()) {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, playerId);
            }
        }
    }
}
