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

/** Covers the source when it is still one of the creatures its ability affects. */
@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesSelfEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardType chosenType = context.source().getChosenCardType();
        if (chosenType != null
                && support.isEffectivelyCreature(context.gameData(), context.source(),
                support.hasAnimateArtifactEffect(context.gameData()))) {
            accumulator.addGrantedEffect(new ProtectionFromCardTypesEffect(Set.of(chosenType)));
        }
    }
}
