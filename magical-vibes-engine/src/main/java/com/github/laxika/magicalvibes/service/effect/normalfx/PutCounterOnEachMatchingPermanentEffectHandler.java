package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Puts counters on each permanent matching the effect's predicate across the battlefields chosen by
 * its {@link EachPermanentScope}. Covers "each attacking creature", "each other creature", "each
 * creature target player controls" and "each creature". +1/+1 self-triggers fire after all
 * placements (rules-correct); -1/-1 placement respects prevention.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnEachMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnEachMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnEachMatchingPermanentEffect) effect;

        List<Permanent> candidates = new ArrayList<>();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getTargetId());
            if (battlefield != null) candidates.addAll(battlefield);
        } else {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                candidates.addAll(battlefield);
            }
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        int count = 0;
        List<Permanent> plusOneTargets = new ArrayList<>();
        // Vizier of Remedies reduces per creature by its own controller's copies, so the placed -1/-1
        // amount can differ per permanent; remember each so the trigger fires the right number of times.
        Map<Permanent, Integer> minusOneTargets = new LinkedHashMap<>();
        for (Permanent p : candidates) {
            if (!predicateEvaluationService.matchesPermanentPredicate(p, e.predicate(), ctx)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;
            int placed = amount;
            if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) continue;
                placed = gameQueryService.reduceMinusOneMinusOneCounters(gameData, p, amount);
                if (placed <= 0) continue;
            }

            p.setCounterCount(e.counterType(), p.getCounterCount(e.counterType()) + placed);
            count++;
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE && amount > 0) {
                plusOneTargets.add(p);
            } else if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                minusOneTargets.put(p, placed);
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = amount == 1 ? "a " + counterName + " counter" : amount + " " + counterName + " counters";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" puts " + counterText + " on " + count + " creature(s).").build());
        log.info("Game {} - {} puts {} {} counter(s) on {} matching permanent(s)", gameData.id,
                entry.getCard().getName(), amount, counterName, count);

        for (Permanent p : plusOneTargets) {
            permanentCounterSupport.firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, p);
        }
        for (Map.Entry<Permanent, Integer> placement : minusOneTargets.entrySet()) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, placement.getKey(), placement.getValue());
        }
    }
}
