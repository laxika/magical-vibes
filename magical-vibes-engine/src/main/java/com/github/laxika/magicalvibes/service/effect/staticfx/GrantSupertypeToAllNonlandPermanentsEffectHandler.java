package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToAllNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/** Grants the configured supertype to every nonland permanent on the battlefield. */
@Component
public class GrantSupertypeToAllNonlandPermanentsEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSupertypeToAllNonlandPermanentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.target().getCard().hasType(CardType.LAND)) {
            accumulator.addGrantedSupertype(
                    ((GrantSupertypeToAllNonlandPermanentsEffect) effect).supertype());
        }
    }
}
