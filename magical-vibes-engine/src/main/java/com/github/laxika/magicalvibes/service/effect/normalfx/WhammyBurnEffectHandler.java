package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WhammyBurnEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Whammy Burn's first reveal. */
@Component
@RequiredArgsConstructor
public class WhammyBurnEffectHandler implements NormalEffectHandlerBean {

    private final WhammyBurnSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WhammyBurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.start(gameData, entry);
    }
}
