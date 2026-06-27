package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerToughnessEqualToControlledCreatureCountSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PowerToughnessEqualToControlledCreatureCountEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());
        int count = support.countControlledPermanents(context,
                p -> support.isEffectivelyCreature(p, hasAnimateArtifacts));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }
}
