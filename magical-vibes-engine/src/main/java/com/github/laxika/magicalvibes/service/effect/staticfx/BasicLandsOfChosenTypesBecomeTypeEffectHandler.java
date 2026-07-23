package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BasicLandsOfChosenTypesBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link BasicLandsOfChosenTypesBecomeTypeEffect}: basic lands carrying the source's
 * first chosen type become the second chosen type (rule 305.7). Managed by the layer-4 pass;
 * this handler is the unmanaged fallback / LayerClassifier registration.
 */
@Component
public class BasicLandsOfChosenTypesBecomeTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BasicLandsOfChosenTypesBecomeTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var from = context.source().getChosenSubtype();
        var to = context.source().getSecondChosenSubtype();
        if (from == null || to == null) {
            return;
        }
        var card = context.target().getCard();
        if (card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC)
                && card.getSubtypes().contains(from)) {
            accumulator.addGrantedSubtype(to);
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
