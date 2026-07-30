package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenTypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Grants the chosen-type protection to the <em>other</em> matching creatures the source's
 * controller controls; {@link GrantProtectionFromChosenTypeToOwnCreaturesSelfEffectHandler} covers
 * the source permanent itself.
 */
@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenTypeToOwnCreaturesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenTypeToOwnCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantProtectionFromChosenTypeToOwnCreaturesEffect) effect;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (support.matchesCreatureScope(context, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(grant.recipientSubtype()))) {
            accumulator.addGrantedEffect(new ProtectionFromSubtypesEffect(Set.of(chosenSubtype), true));
        }
    }
}
