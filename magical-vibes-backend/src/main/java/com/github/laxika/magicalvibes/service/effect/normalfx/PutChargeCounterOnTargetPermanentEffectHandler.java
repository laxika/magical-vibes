package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutChargeCounterOnTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PutCounterOnTargetPermanentEffectHandler delegate;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutChargeCounterOnTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        delegate.resolve(gameData, entry, new PutCounterOnTargetPermanentEffect(CounterType.CHARGE));
    }
}
