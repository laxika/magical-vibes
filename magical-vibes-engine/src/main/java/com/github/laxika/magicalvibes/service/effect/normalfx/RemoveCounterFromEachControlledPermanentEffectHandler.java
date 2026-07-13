package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
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
            p.setCounterCount(e.counterType(), Math.max(0, current - e.amount()));
            count++;
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = e.amount() == 1 ? "a " + counterName + " counter" : e.amount() + " " + counterName + " counters";
        String logEntry = entry.getCard().getName() + " removes " + counterText + " from " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} removes {} {} counter(s) from {} controlled permanent(s)", gameData.id,
                entry.getCard().getName(), e.amount(), counterName, count);
    }
}
