package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsAndSearchBasicLandPerControllerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileAllPermanentsAndSearchBasicLandPerControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllPermanentsAndSearchBasicLandPerControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileAllPermanentsAndSearchBasicLandPerControllerEffect) effect;
        List<Permanent> toExile = new ArrayList<>();
        Map<UUID, Integer> exiledByController = new HashMap<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, exile.filter(), filterContext)) {
                    toExile.add(permanent);
                    exiledByController.merge(controllerId, 1, Integer::sum);
                }
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, permanent);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}",
                        gameData.id, permanent.getCard().getName(), entry.getCard().getName());
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        List<UUID> searchers = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            for (int i = 0; i < exiledByController.getOrDefault(playerId, 0); i++) {
                searchers.add(playerId);
            }
        }
        if (!searchers.isEmpty()) {
            librarySearchSupport.startNextEachPlayerBasicLandSearch(gameData,
                    LibrarySearchFollowUp.eachPlayerBasicLand(searchers, true), false);
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> result = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            result.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                result.add(playerId);
            }
        }
        return result;
    }
}
