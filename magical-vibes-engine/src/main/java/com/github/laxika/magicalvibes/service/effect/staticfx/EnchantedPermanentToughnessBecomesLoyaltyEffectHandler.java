package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentToughnessBecomesLoyaltyEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/** Supplies the enchanted permanent's base toughness from its toughness-as-loyalty state. */
@Component
public class EnchantedPermanentToughnessBecomesLoyaltyEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentToughnessBecomesLoyaltyEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }
        int toughness = context.target().getToughnessAsLoyalty() != null
                ? context.target().getToughnessAsLoyalty()
                : context.target().getBaseToughness();
        // Planeswalkerificate changes toughness only; the creature's power remains unchanged.
        accumulator.setBasePTOverride(context.target().getBasePower(), toughness);
    }
}
