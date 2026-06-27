package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared bounce helpers used by every "normal" Bounce effect handler.
 *
 * <p>Extracted verbatim from {@code BounceResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BounceSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    public void applyReturnSelfToHand(GameData gameData, StackEntry entry) {
        Permanent toReturn = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        if (toReturn == null) {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        permanentRemovalService.removePermanentToHand(gameData, toReturn);
        permanentRemovalService.removeOrphanedAuras(gameData);

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }
}
