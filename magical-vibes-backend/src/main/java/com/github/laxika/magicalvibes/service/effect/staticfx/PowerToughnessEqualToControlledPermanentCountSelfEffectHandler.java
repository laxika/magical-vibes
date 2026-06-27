package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerToughnessEqualToControlledPermanentCountSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PowerToughnessEqualToControlledPermanentCountEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pt = (PowerToughnessEqualToControlledPermanentCountEffect) effect;
        int count = support.countControlledPermanents(context,
                p -> gameQueryService.matchesPermanentPredicate(context.gameData(), p, pt.filter()));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }
}
