package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureByColorCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostEnchantedCreatureByColorCountEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEnchantedCreatureByColorCountEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        BoostEnchantedCreatureByColorCountEffect boost = (BoostEnchantedCreatureByColorCountEffect) effect;
        if (!support.matchesCreatureScope(context, GrantScope.ENCHANTED_CREATURE, null)) {
            return;
        }
        Permanent target = context.target();
        int colorCount = support.effectiveColorCount(target);
        accumulator.addPower(boost.powerPerColor() * colorCount);
        accumulator.addToughness(boost.toughnessPerColor() * colorCount);
    }
}
