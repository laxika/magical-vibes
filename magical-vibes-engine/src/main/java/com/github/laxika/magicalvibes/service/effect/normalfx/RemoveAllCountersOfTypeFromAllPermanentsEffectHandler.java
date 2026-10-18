package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersOfTypeFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Removes every counter of the given type from every permanent on every battlefield that matches
 * the effect's predicate (Corrosion leaves-the-battlefield cleanup, Quicksilver Fountain land cleanup).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersOfTypeFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersOfTypeFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersOfTypeFromAllPermanentsEffect) effect;
        int affected = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent p : new ArrayList<>(battlefield)) {
                int current = p.getCounterCount(e.counterType());
                if (current <= 0) continue;
                if (!predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.predicate())) continue;
                p.setCounterCount(e.counterType(), 0);
                if (e.counterType() == CounterType.OIL) {
                    gameData.recordOilCounterRemoved(p, current);
                }
                affected++;
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" removes all " + counterName + " counters from all permanents ("
                        + affected + " permanent(s)).")
                .build());
        log.info("Game {} - {} removes all {} counters from {} permanent(s)",
                gameData.id, entry.getCard().getName(), counterName, affected);
    }
}
