package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnEachControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnEachControlledPermanentEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        int count = 0;
        List<Permanent> plusOneTargets = new ArrayList<>();
        List<Permanent> minusOneTargets = new ArrayList<>();
        for (Permanent p : new ArrayList<>(battlefield)) {
            if (!predicateEvaluationService.matchesPermanentPredicate(p, e.predicate(), ctx)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;
            if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) continue;

            p.setCounterCount(e.counterType(), p.getCounterCount(e.counterType()) + amount);
            count++;
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE && amount > 0) {
                plusOneTargets.add(p);
            } else if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE && amount > 0) {
                minusOneTargets.add(p);
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = amount == 1 ? "a " + counterName + " counter" : amount + " " + counterName + " counters";
        String logEntry = entry.getCard().getName() + " puts " + counterText + " on " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" puts " + counterText + " on " + count + " permanent(s) you control.").build());
        log.info("Game {} - {} puts {} {} counter(s) on {} controlled permanent(s)", gameData.id,
                entry.getCard().getName(), amount, counterName, count);

        // Fire ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT triggers after all placements (rules-correct).
        // Deferred past the loop since firing pushes triggered abilities onto the stack.
        for (Permanent p : plusOneTargets) {
            permanentCounterSupport.firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, p);
        }
        for (Permanent p : minusOneTargets) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, p, amount);
        }
    }
}
