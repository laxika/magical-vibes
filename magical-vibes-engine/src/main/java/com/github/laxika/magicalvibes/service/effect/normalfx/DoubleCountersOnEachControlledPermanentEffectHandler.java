package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleCountersOnEachControlledPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleCountersOnEachControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        DoubleCountersOnEachControlledPermanentEffect e =
                (DoubleCountersOnEachControlledPermanentEffect) effect;
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        int doubledCount = 0;

        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), context)) {
                continue;
            }

            boolean doubledAny = false;
            for (CounterType counterType : CounterType.values()) {
                if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                    continue;
                }
                int current = permanent.getCounterCount(counterType);
                if (current <= 0) {
                    continue;
                }
                if (permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, permanent, counterType, current) > 0) {
                    doubledAny = true;
                }
            }
            if (doubledAny) {
                doubledCount++;
            }
        }

        if (doubledCount == 0) {
            return;
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" doubles counters on " + doubledCount + " permanent(s) you control.").build());
        log.info("Game {} - {} doubled counters on {} controlled permanent(s)", gameData.id,
                entry.getCard().getName(), doubledCount);
    }
}
