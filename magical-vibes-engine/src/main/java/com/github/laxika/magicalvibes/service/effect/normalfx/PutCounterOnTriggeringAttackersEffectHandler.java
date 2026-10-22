package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTriggeringAttackersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Puts one counter on each creature bound by {@link PutCounterOnTriggeringAttackersEffect}. A creature
 * that has left the battlefield by resolution is skipped; one that stopped attacking still gets it.
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnTriggeringAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnTriggeringAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnTriggeringAttackersEffect) effect;
        CounterType counterType = e.counterType();
        for (AttackingPermanentSnapshot attacker : e.attackingCreatures()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, attacker.permanentId());
            if (permanent == null || gameQueryService.cantHaveCounters(gameData, permanent)) continue;
            if (counterType == CounterType.PLUS_ONE_PLUS_ONE
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) continue;
            int placed = gameQueryService.replaceCounters(gameData, permanent, counterType, 1,
                    entry.getControllerId());
            if (placed <= 0) continue;

            permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) + placed);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, permanent, placed, counterType);
            if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                        gameData, permanent, entry.getControllerId());
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, permanent, placed);
            }
        }
    }
}
