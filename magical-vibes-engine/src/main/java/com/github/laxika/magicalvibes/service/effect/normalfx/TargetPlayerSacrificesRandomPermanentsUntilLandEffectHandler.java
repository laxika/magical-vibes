package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesRandomPermanentsUntilLandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerSacrificesRandomPermanentsUntilLandEffect}: the targeted player
 * repeatedly sacrifices a uniformly-random permanent they control, stopping once a land is
 * sacrificed or they control no permanents (Tyrant of Discord).
 *
 * <p>The choice is made by the engine, so no player input is needed and the loop runs to
 * completion inside this resolution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesRandomPermanentsUntilLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesRandomPermanentsUntilLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.info("Game {} - {} fizzles (no valid target player)", gameData.id, entry.getCard().getName());
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        boolean repeat = true;
        while (repeat) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield == null || battlefield.isEmpty()) {
                return;
            }

            Permanent chosen = battlefield.get(ThreadLocalRandom.current().nextInt(battlefield.size()));
            repeat = !gameQueryService.isLand(gameData, chosen);

            if (!permanentRemovalService.removePermanentToGraveyard(gameData, chosen)) {
                return;
            }
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, targetPlayerId, chosen.getCard());
            gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", chosen.getCard(), "."));
            log.info("Game {} - {} sacrifices {} to {}", gameData.id, playerName, chosen.getCard().getName(),
                    entry.getCard().getName());
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
