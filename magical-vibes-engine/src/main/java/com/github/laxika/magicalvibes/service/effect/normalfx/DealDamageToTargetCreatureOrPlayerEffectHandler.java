package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves creature-or-player damage through the shared any-target damage pipeline. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToAnyTargetEffectHandler delegate;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetCreatureOrPlayerEffect) effect;
        delegate.resolve(gameData, entry, new DealDamageToAnyTargetEffect(damageEffect.damage()));
    }
}
