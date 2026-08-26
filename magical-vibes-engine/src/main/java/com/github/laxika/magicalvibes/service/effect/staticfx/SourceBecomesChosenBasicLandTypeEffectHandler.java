package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenBasicLandTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class SourceBecomesChosenBasicLandTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceBecomesChosenBasicLandTypeEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null || !context.source().getCard().hasType(CardType.LAND)) {
            return;
        }
        accumulator.addGrantedSubtype(chosenSubtype);
        accumulator.setSubtypeOverriding(true);
        accumulator.setLandSubtypeOverriding(true);
    }
}
