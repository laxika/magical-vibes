package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantEffectEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if (support.namesBeyondSource(context, StaticEffectSupport.withAllPermanentsIncludingSource(grant.scope()),
                grant.filter())) {
            CardEffect granted = bindToSource(grant.effect(), context.source());
            if (granted != null) {
                accumulator.addGrantedEffect(granted);
            }
        }
    }

    /**
     * Fixes a granted ability's reference to its source's choice. Landwalk "of the chosen type"
     * (Traveler's Cloak) names the land type the Aura's controller chose as the Aura entered; the
     * creature that has the ability made no choice, so the type is bound before the grant. Returns
     * {@code null} when the source made no choice, since the ability names no land type.
     */
    private static CardEffect bindToSource(CardEffect effect, Permanent source) {
        if (effect instanceof CantBeBlockedIfDefenderControlsMatchingPermanentEffect landwalk
                && landwalk.defenderPermanentPredicate() instanceof PermanentHasSourceChosenSubtypePredicate) {
            CardSubtype chosen = source.getChosenSubtype();
            return chosen == null ? null : new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                    new PermanentHasSubtypePredicate(chosen), landwalk.landwalk());
        }
        return effect;
    }
}
