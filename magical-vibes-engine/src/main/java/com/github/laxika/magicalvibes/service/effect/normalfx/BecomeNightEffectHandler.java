package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.turn.DayNightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeNightEffectHandler implements NormalEffectHandlerBean {

    private final DayNightService dayNightService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeNightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        switch (((BecomeNightEffect) effect).change()) {
            case NIGHT -> dayNightService.becomeNight(gameData);
            case DAY -> dayNightService.becomeDay(gameData);
            case TOGGLE -> dayNightService.toggle(gameData);
        }
    }
}
