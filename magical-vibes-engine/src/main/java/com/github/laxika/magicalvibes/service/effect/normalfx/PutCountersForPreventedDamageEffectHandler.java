package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersForPreventedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCountersForPreventedDamageEffectHandler implements NormalEffectHandlerBean {

    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;

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
                    boon.permanentId(), 0, boon.controllerId(), boon.sourceCard(), boon.shieldId(),
                    boon.remainingShield(), true));
        }
        if (boon.totalCounters() > 0) {
            putCountersOnSourceEffectHandler.resolve(gameData, entry,
                    new PutCountersOnSourceEffect(0, 1, boon.totalCounters()));
        }
    }
}
