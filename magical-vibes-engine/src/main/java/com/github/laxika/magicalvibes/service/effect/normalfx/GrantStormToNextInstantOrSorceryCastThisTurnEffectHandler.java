package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;
import org.springframework.stereotype.Component;

@Component
public class GrantStormToNextInstantOrSorceryCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStormToNextInstantOrSorceryCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.pendingNextInstantSorceryStormThisTurnCount
                .merge(entry.getControllerId(), 1, Integer::sum);
    }
}
