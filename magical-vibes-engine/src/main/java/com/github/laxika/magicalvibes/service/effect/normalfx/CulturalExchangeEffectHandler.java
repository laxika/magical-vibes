package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CulturalExchangeEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CulturalExchangeEffectHandler implements NormalEffectHandlerBean {

    private final CulturalExchangeSupport culturalExchangeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CulturalExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        culturalExchangeSupport.begin(gameData, entry);
    }
}
