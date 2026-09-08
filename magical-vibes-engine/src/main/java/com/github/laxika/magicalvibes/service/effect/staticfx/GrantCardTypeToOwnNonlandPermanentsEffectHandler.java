package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantCardTypeToOwnNonlandPermanentsEffectHandler implements StaticEffectHandlerBean {

    private static final PermanentPredicate NONLAND =
            new PermanentNotPredicate(new PermanentIsLandPredicate());

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantCardTypeToOwnNonlandPermanentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantCardTypeToOwnNonlandPermanentsEffect) effect;
        if (context.targetOnSameBattlefield()
                && support.matchesStaticFilter(context, context.target(), NONLAND)) {
            accumulator.addGrantedCardType(grant.cardType());
        }
    }
}
