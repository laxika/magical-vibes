package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenTypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Self half of {@link GrantProtectionFromChosenTypeToOwnCreaturesEffectHandler}: "Human creatures
 * you control" includes the source itself when it still has the recipient subtype.
 */
@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenTypeToOwnCreaturesSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenTypeToOwnCreaturesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantProtectionFromChosenTypeToOwnCreaturesEffect) effect;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (support.matchesStaticFilter(context.target(),
                new PermanentHasSubtypePredicate(grant.recipientSubtype()))) {
            accumulator.addGrantedEffect(new ProtectionFromSubtypesEffect(Set.of(chosenSubtype), true));
        }
    }
}
