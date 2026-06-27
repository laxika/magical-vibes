package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByImprintedCreaturePTEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class BoostSelfByImprintedCreaturePTSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfByImprintedCreaturePTEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null || imprintedCard.getPower() == null || imprintedCard.getToughness() == null) {
            return;
        }
        accumulator.addPower(imprintedCard.getPower());
        accumulator.addToughness(imprintedCard.getToughness());
    }
}
