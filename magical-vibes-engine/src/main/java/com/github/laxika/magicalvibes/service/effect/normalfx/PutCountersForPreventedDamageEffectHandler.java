package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersForPreventedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCountersForPreventedDamageEffectHandler implements NormalEffectHandlerBean {

    private final PutCounterOnTargetPermanentEffectHandler putCounterOnTargetPermanentEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersForPreventedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersForPreventedDamageEffect) effect;
        DelayedPlusZeroPlusOneCounters boon = gameData.takeSacredBoonEntry(e.shieldId());
        if (boon == null) {
            return;
        }
        if (boon.remainingShield() > 0) {
            gameData.requeueSacredBoonEntry(new DelayedPlusZeroPlusOneCounters(
                    boon.permanentId(), 0, boon.shieldId(), boon.remainingShield(), true));
        }
        if (boon.totalCounters() > 0) {
            putCounterOnTargetPermanentEffectHandler.resolve(gameData, entry,
                    new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ZERO_PLUS_ONE, boon.totalCounters()));
        }
    }
}
