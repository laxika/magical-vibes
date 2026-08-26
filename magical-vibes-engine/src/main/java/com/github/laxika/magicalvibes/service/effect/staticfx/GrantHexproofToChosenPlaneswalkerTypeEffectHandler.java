package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHexproofToChosenPlaneswalkerTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantHexproofToChosenPlaneswalkerTypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantHexproofToChosenPlaneswalkerTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null || !context.targetOnSameBattlefield()) return;
        if (!support.matchesStaticFilter(context, context.target(), new PermanentIsPlaneswalkerPredicate())) return;
        if (support.matchesStaticLeaf(context.target(), new PermanentHasSubtypePredicate(chosenSubtype))) {
            accumulator.addKeyword(Keyword.HEXPROOF);
        }
    }
}
