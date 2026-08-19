package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterFromEachControlledPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromEachControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromEachControlledPermanentEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        int count = 0;
        for (Permanent p : new ArrayList<>(battlefield)) {
            if (!predicateEvaluationService.matchesPermanentPredicate(p, e.predicate(), ctx)) continue;
            int current = p.getCounterCount(e.counterType());
            if (current <= 0) continue;
            int removed = Math.min(current, e.amount());
            p.setCounterCount(e.counterType(), current - removed);
            if (e.counterType() == CounterType.OIL) {
                gameData.recordOilCounterRemoved(p, removed);
            }
            count++;
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = e.amount() == 1 ? "a " + counterName + " counter" : e.amount() + " " + counterName + " counters";
        
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" removes " + counterText + " from " + count + " permanent(s) you control.").build());
        log.info("Game {} - {} removes {} {} counter(s) from {} controlled permanent(s)", gameData.id,
                entry.getCard().getName(), e.amount(), counterName, count);
    }
}
