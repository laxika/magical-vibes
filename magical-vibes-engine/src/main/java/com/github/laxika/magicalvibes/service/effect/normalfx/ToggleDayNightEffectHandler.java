package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ToggleDayNightEffect;
import com.github.laxika.magicalvibes.service.turn.DayNightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToggleDayNightEffectHandler implements NormalEffectHandlerBean {

    private final DayNightService dayNightService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ToggleDayNightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        dayNightService.toggle(gameData);
    }
}
