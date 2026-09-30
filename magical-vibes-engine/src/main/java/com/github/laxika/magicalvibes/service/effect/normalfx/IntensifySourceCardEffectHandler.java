package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import org.springframework.stereotype.Component;

/** Resolves an intensity increase on one physical card identity. */
@Component
public class IntensifySourceCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntensifySourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IntensifySourceCardEffect intensify = (IntensifySourceCardEffect) effect;
        gameData.intensifyCard(entry.getCard(), intensify.amount());
    }
}
