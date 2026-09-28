package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfEventValueEqualsEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WinGameIfEventValueEqualsEffectHandler implements NormalEffectHandlerBean {

    private final WinGameEffectHandler winGameEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameIfEventValueEqualsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        WinGameIfEventValueEqualsEffect conditional = (WinGameIfEventValueEqualsEffect) effect;
        if (entry.getEventValue() == conditional.expectedValue()) {
            winGameEffectHandler.resolve(gameData, entry, new WinGameEffect());
        }
    }
}
