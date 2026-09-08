package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect;
import org.springframework.stereotype.Component;

/** Registers Soulfire Grand Master's one-shot return-to-hand replacement. */
@Component
public class ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnNextInstantOrSorceryCastFromHandToHandThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.pendingNextInstantSorceryCastFromHandToHandThisTurnCount
                .merge(entry.getControllerId(), 1, Integer::sum);
    }
}
