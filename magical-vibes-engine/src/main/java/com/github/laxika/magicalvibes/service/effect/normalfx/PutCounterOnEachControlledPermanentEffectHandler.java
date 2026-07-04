package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnEachControlledPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnEachControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnEachControlledPermanentEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        int count = 0;
        List<Permanent> plusOneTargets = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(p, e.predicate(), ctx)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;
            if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) continue;

            p.setCounterCount(e.counterType(), p.getCounterCount(e.counterType()) + e.count());
            count++;
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE && e.count() > 0) {
                plusOneTargets.add(p);
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = e.count() == 1 ? "a " + counterName + " counter" : e.count() + " " + counterName + " counters";
        String logEntry = entry.getCard().getName() + " puts " + counterText + " on " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} {} counter(s) on {} controlled permanent(s)", gameData.id,
                entry.getCard().getName(), e.count(), counterName, count);

        // Fire ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT triggers after all placements (rules-correct).
        // Deferred past the loop since firing pushes triggered abilities onto the stack.
        for (Permanent p : plusOneTargets) {
            permanentCounterSupport.firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, p);
        }
    }
}
