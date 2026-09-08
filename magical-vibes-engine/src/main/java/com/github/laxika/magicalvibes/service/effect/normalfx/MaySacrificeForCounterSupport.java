package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared steps of {@code MaySacrificePermanentForCounterOrTapSourceEffect}, used by the resolution
 * handler, the accept/decline handler and the "which permanent" choice completion.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaySacrificeForCounterSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DestructionSupport destructionSupport;

    /** The permanents {@code controllerId} controls that may be sacrificed. */
    public List<UUID> matchingPermanentIds(GameData gameData, UUID controllerId, PermanentPredicate filter) {
        return matchingPermanentIds(gameData, controllerId, null, filter);
    }

    /** The matching permanents, evaluated with the source identity available to relative predicates. */
    public List<UUID> matchingPermanentIds(GameData gameData, UUID controllerId,
                                           UUID sourcePermanentId, PermanentPredicate filter) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId)
                .withSourceCardId(source == null ? null : source.getCard().getId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context)) {
                    ids.add(permanent.getId());
                }
            }
        }
        return ids;
    }

    /**
     * "If you do, put a +1/+1 counter on this creature" — sacrifices {@code permanentId} and puts the
     * counter on the source. Sacrificing the source itself leaves nothing to put a counter on.
     */
    public void sacrificeThenAddCounter(GameData gameData, UUID controllerId, UUID permanentId, UUID sourcePermanentId) {
        sacrificeThenAddCounter(gameData, controllerId, permanentId, sourcePermanentId,
                CounterType.PLUS_ONE_PLUS_ONE);
    }

    /** Sacrifices {@code permanentId} and puts one counter of {@code counterType} on the source. */
    public void sacrificeThenAddCounter(GameData gameData, UUID controllerId, UUID permanentId,
                                        UUID sourcePermanentId, CounterType counterType) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            return;
        }
        destructionSupport.sacrificeAndLog(gameData, toSacrifice, controllerId);

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, source, 1);
            } else {
                permanentCounterSupport.placeCounterOnPermanent(gameData, null, source, counterType, 1);
            }
        }
    }

    /** Removes one counter of {@code counterType} from the source, if it has one. */
    public void removeCounterFromSource(GameData gameData, UUID sourcePermanentId, CounterType counterType) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            permanentCounterSupport.removeCounterFromPermanent(gameData, source, counterType, 1);
        }
    }

    /** "If you don't, tap this creature." */
    public void tapSource(GameData gameData, UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || source.isTapped()) {
            return;
        }
        source.tap();
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is tapped."));
        log.info("Game {} - {} is tapped (no sacrifice made)", gameData.id, source.getCard().getName());
    }
}
