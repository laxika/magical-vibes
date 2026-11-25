package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersForPreventedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCountersForPreventedDamageEffectHandler implements NormalEffectHandlerBean {

    private final PutCountersEffectHandler putCountersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersForPreventedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersForPreventedDamageEffect) effect;
        int counters = gameData.takeSacredBoonTally(e.shieldId());
        if (counters > 0) {
            putCountersEffectHandler.resolve(gameData, entry,
                    new PutCountersEffect(CounterType.PLUS_ZERO_PLUS_ONE, counters));
        }
    }
}
