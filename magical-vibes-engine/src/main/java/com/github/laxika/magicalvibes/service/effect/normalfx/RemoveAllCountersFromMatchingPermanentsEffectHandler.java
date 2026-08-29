package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link RemoveAllCountersFromMatchingPermanentsEffect} across all battlefields.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromMatchingPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromMatchingPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersFromMatchingPermanentsEffect) effect;
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        int affected = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), context)) {
                    continue;
                }
                if (permanent.getCounterCount(e.counterType()) <= 0) {
                    continue;
                }
                int removed = permanent.getCounterCount(e.counterType());
                permanent.setCounterCount(e.counterType(), 0);
                if (e.counterType() == CounterType.OIL) {
                    gameData.recordOilCounterRemoved(permanent, removed);
                }
                affected++;
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" removes all " + counterName + " counters from " + affected
                        + " matching permanent(s).")
                .build());
        log.info("Game {} - {} removes all {} counters from {} matching permanent(s)",
                gameData.id, entry.getCard().getName(), counterName, affected);
    }
}
