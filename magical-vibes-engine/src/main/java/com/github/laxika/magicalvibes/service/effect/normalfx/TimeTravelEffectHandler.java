package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a time-travel event sequence. */
@Component
@RequiredArgsConstructor
public class TimeTravelEffectHandler implements NormalEffectHandlerBean {

    private final TimeTravelService timeTravelService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimeTravelEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        timeTravelService.begin(gameData, entry, ((TimeTravelEffect) effect).times());
    }
}
