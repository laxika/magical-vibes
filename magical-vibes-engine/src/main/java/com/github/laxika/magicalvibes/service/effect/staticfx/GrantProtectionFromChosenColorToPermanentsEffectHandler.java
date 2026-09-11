package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenColorToPermanentsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenColorToPermanentsEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenColorToPermanentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantProtectionFromChosenColorToPermanentsEffect) effect;
        CardColor chosenColor = context.source().getChosenColor();
        if (chosenColor != null && support.matchesStaticFilter(context, context.target(), grant.filter())) {
            accumulator.addProtectionColors(Set.of(chosenColor));
        }
    }
}
