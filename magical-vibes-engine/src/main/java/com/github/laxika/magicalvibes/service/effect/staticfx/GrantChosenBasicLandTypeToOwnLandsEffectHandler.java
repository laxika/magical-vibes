package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenBasicLandTypeToOwnLandsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Grants the intrinsic mana ability associated with Realmwright's chosen basic land type.
 */
@Component
@RequiredArgsConstructor
public class GrantChosenBasicLandTypeToOwnLandsEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenBasicLandTypeToOwnLandsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null || !support.matchesLandScope(context, GrantScope.OWN_LANDS,
                new PermanentHasSourceChosenSubtypePredicate())) {
            return;
        }

        accumulator.addActivatedAbility(ManaAbilities.tapFor(
                EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(chosenSubtype)));
    }
}
