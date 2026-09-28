package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffectSelfHandler
        implements StaticEffectHandlerBean {

    private final GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffectHandler delegate;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilitiesOfLandCardsExiledWithSourceEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        delegate.apply(context, effect, accumulator);
    }
}
