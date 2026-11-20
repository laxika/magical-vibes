package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Applies the creature portion of Serra's Emissary's chosen-type protection. */
@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardType chosenType = context.source().getChosenCardType();
        if (chosenType != null
                && support.names(context, StaticEffectSupport.OTHER_CREATURES_YOU_CONTROL, null)) {
            accumulator.addGrantedEffect(new ProtectionFromCardTypesEffect(Set.of(chosenType)));
        }
    }
}
