package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves counter removal over the battlefield selected by the effect's scope. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterFromEachMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromEachMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromEachMatchingPermanentEffect) effect;
        List<Permanent> candidates = new ArrayList<>();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getTargetId());
            if (battlefield != null) candidates.addAll(battlefield);
        } else {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                candidates.addAll(battlefield);
            }
        }

        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : candidates) {
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), context)) {
                continue;
            }
            int current = permanent.getCounterCount(e.counterType());
            if (current <= 0) {
                continue;
            }
            int removed = Math.min(current, e.amount());
            permanent.setCounterCount(e.counterType(), current - removed);
            if (e.counterType() == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
            count++;
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = e.amount() == 1
                ? "a " + counterName + " counter"
                : e.amount() + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" removes " + counterText + " from " + count + " matching permanent(s).")
                .build());
        log.info("Game {} - {} removes {} {} counter(s) from {} matching permanent(s)",
                gameData.id, entry.getCard().getName(), e.amount(), counterName, count);
    }
}
