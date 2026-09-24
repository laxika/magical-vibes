package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Oversimplify's creature exile and per-player Fractal creation. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect) effect;
        List<Permanent> toExile = new ArrayList<>();
        Map<UUID, Integer> totalPowerByController = new LinkedHashMap<>();

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                toExile.add(permanent);
                totalPowerByController.merge(
                        controllerId, gameQueryService.getEffectivePower(gameData, permanent), Integer::sum);
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, permanent);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}", gameData.id, permanent.getCard().getName(),
                        entry.getCard().getName());
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                    gameData, playerId, e.tokenTemplate().withAmount(1), entry.getCard().getSetCode());
            entry.getCreatedPermanentIds().addAll(createdIds);
            int counterAmount = Math.max(0, totalPowerByController.getOrDefault(playerId, 0));
            if (counterAmount == 0 || createdIds.isEmpty()) {
                continue;
            }

            Permanent token = gameQueryService.findPermanentById(gameData, createdIds.getLast());
            if (token != null && !gameQueryService.cantHaveCounters(gameData, token)) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, token, e.counterType(), counterAmount);
            }
        }
    }
}
