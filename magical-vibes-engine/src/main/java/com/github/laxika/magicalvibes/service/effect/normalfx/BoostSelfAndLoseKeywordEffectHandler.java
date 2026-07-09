package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfAndLoseKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BoostSelfAndLoseKeywordEffect} by delegating to the existing
 * {@link BoostSelfEffect} and {@link RemoveKeywordEffect} (SELF scope) handlers.
 */
@Component
@RequiredArgsConstructor
public class BoostSelfAndLoseKeywordEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfAndLoseKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostSelfAndLoseKeywordEffect) effect;

        var boost = new BoostSelfEffect(e.powerBoost(), e.toughnessBoost());
        effectHandlerRegistry.getHandler(boost).resolve(gameData, entry, boost);

        var remove = new RemoveKeywordEffect(e.keyword(), GrantScope.SELF);
        effectHandlerRegistry.getHandler(remove).resolve(gameData, entry, remove);
    }
}
