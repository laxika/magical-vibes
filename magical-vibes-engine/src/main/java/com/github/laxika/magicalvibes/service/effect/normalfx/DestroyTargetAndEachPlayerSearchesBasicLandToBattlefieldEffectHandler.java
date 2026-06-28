package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect) effect);
    }

    private void doResolve(
            GameData gameData, StackEntry entry,
            DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Attempt to destroy the permanent
        if (permanentRemovalService.tryDestroyPermanent(gameData, target, false)) {
            String logEntry = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        // Build APNAP-ordered queue: active player first, then others in turn order
        gameData.pendingEachPlayerBasicLandSearchQueue.clear();
        gameData.pendingEachPlayerBasicLandSearchTapped = false;
        UUID activePlayerId = gameData.activePlayerId;
        gameData.pendingEachPlayerBasicLandSearchQueue.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerBasicLandSearchQueue.add(playerId);
            }
        }

        // Start the first player's search
        librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData);
    }
}
