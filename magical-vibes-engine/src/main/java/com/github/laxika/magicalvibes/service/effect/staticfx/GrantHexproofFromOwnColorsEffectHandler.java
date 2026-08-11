package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHexproofFromOwnColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GrantHexproofFromOwnColorsEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantHexproofFromOwnColorsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!support.matchesCreatureScope(context, GrantScope.OWN_CREATURES, null)) {
            return;
        }
        Set<CardColor> colors = gameQueryService.colorsForStaticEvaluation(context.target());
        if (!colors.isEmpty()) {
            accumulator.addGrantedEffect(TargetingRestrictionEffect.hexproofFromColors(colors));
        }
    }
}
