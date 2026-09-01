package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrPutCounterIfTreasureManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostSelfOrPutCounterIfTreasureManaEffectHandler implements NormalEffectHandlerBean {

    private final BoostSelfEffectHandler boostSelfEffectHandler;
    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfOrPutCounterIfTreasureManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.isActivationUsedTreasureMana()) {
            putCountersOnSourceEffectHandler.resolve(gameData, entry, new PutCountersOnSourceEffect(1, 1, 1));
        } else {
            boostSelfEffectHandler.resolve(gameData, entry, new BoostSelfEffect(1, 1));
        }
    }
}
