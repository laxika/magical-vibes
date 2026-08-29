package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessStaticSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var setPT = (SetBasePowerToughnessEffect) effect;
        boolean applies = setPT.scope() == GrantScope.SELF
                ? support.matchesStaticFilter(context, context.target(), setPT.filter())
                : support.matchesCreatureScope(context, setPT.scope(), setPT.filter());
        if (applies) {
            accumulator.setBasePTOverride(setPT.power(), setPT.toughness());
        }
    }
}
