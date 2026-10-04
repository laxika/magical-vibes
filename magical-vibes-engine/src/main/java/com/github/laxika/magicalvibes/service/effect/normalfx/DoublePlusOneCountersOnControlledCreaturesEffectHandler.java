package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnControlledCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoublePlusOneCountersOnControlledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoublePlusOneCountersOnControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        List<Permanent> doubled = new ArrayList<>();
        DoublePlusOneCountersOnControlledCreaturesEffect doublingEffect =
                (DoublePlusOneCountersOnControlledCreaturesEffect) effect;
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            if (doublingEffect.predicate() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, doublingEffect.predicate())) continue;
            if (gameQueryService.cantHaveCounters(gameData, permanent)
                    || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) continue;

            int current = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            if (current <= 0) continue;

            int added = current * (MaroGoneNutsSupport.apply(gameData, effect, 2) - 1);
            added = gameQueryService.replaceCounters(gameData, permanent, CounterType.PLUS_ONE_PLUS_ONE,
                    added, entry.getControllerId());
            if (added <= 0) continue;
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, current + added);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, permanent, added,
                    CounterType.PLUS_ONE_PLUS_ONE);
            permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                    gameData, permanent, entry.getControllerId());
            permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                    gameData, permanent, added, entry.getControllerId());
            doubled.add(permanent);
        }

        if (doubled.isEmpty()) {
            return;
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" doubles the number of +1/+1 counters on " + doubled.size() + " creature(s) you control.").build());
        log.info("Game {} - {} doubled +1/+1 counters on {} creature(s)", gameData.id,
                entry.getCard().getName(), doubled.size());

        for (Permanent permanent : doubled) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, permanent, entry.getControllerId());
        }
    }
}
