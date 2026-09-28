package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.AllCreaturesAreTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies Intangible Vibes' token characteristic to every effective creature. */
@Component
@RequiredArgsConstructor
public class AllCreaturesAreTokensEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport staticEffectSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllCreaturesAreTokensEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (staticEffectSupport.isEffectivelyCreature(
                context.gameData(), context.target(),
                context.gameData() != null
                        && staticEffectSupport.hasAnimateArtifactEffect(context.gameData()))) {
            accumulator.setTokenized(true);
        }
    }
}
