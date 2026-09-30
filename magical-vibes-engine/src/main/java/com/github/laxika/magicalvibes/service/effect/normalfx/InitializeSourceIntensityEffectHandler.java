package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import org.springframework.stereotype.Component;

/** Initializes a source card's persistent intensity once. */
@Component
public class InitializeSourceIntensityEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InitializeSourceIntensityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.initializeCardIntensity(entry.getCard(), ((InitializeSourceIntensityEffect) effect).amount());
    }
}
