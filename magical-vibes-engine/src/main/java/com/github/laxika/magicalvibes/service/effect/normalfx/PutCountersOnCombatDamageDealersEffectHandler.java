package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCombatDamageDealersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves counter placement on the creatures captured by a batched combat-damage trigger.
 * +1/+1 counter triggers are queued after all captured dealers have been updated.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCountersOnCombatDamageDealersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnCombatDamageDealersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnCombatDamageDealersEffect) effect;
        int count = 0;
        List<Permanent> plusOneTargets = new ArrayList<>();
        Map<Permanent, Integer> minusOneTargets = new LinkedHashMap<>();

        for (var dealerId : e.combatDamageDealerIds()) {
            Permanent dealer = gameQueryService.findPermanentById(gameData, dealerId);
            if (dealer == null || gameQueryService.cantHaveCounters(gameData, dealer)) {
                continue;
            }
            if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, dealer)) {
                continue;
            }
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, dealer)) {
                continue;
            }

            int placed = gameQueryService.replaceCounters(
                    gameData, dealer, e.counterType(), e.amount(), entry.getControllerId());
            if (placed <= 0) {
                continue;
            }

            dealer.setCounterCount(e.counterType(), dealer.getCounterCount(e.counterType()) + placed);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, dealer, placed);
            count++;
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                        gameData, dealer, entry.getControllerId());
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, dealer, placed);
                plusOneTargets.add(dealer);
            } else if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                minusOneTargets.put(dealer, placed);
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = e.amount() == 1
                ? "a " + counterName + " counter"
                : e.amount() + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" puts " + counterText + " on " + count + " combat damage dealer(s).")
                .build());
        log.info("Game {} - {} puts {} {} counter(s) on {} combat damage dealer(s)",
                gameData.id, entry.getCard().getName(), e.amount(), counterName, count);

        for (Permanent target : plusOneTargets) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(
                    gameData, target, entry.getControllerId());
        }
        for (Map.Entry<Permanent, Integer> placement : minusOneTargets.entrySet()) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                    gameData, placement.getKey(), placement.getValue());
        }
    }
}
