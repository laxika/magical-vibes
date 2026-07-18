package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Contributes the colour, base P/T and creature-ness of an
 * {@link EnchantedPermanentBecomesCreatureEffect} aura to the enchanted permanent's static bonus.
 * The layer-4 type change (creature card type + subtypes) is applied by the layered pass; this
 * handler fills the layer-5 colour and layer-7 base P/T for the accumulator/view.
 */
@Component
public class EnchantedPermanentBecomesCreatureEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentBecomesCreatureEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomes = (EnchantedPermanentBecomesCreatureEffect) effect;
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }
        int power = becomes.power();
        int toughness = becomes.toughness();
        if (becomes.powerToughnessEqualsManaValue()) {
            // Animate Artifact: only while the enchanted artifact isn't already a creature, and
            // base P/T each equal to its mana value.
            if (context.target().getCard().hasType(CardType.CREATURE)) {
                return;
            }
            power = context.target().getCard().getManaValue();
            toughness = power;
        }
        accumulator.setAnimatedCreature(true);
        accumulator.setBasePTOverride(power, toughness);
        accumulator.addGrantedCardType(CardType.CREATURE);
        for (CardSubtype subtype : becomes.subtypes()) {
            accumulator.addGrantedSubtype(subtype);
        }
        if (becomes.color() != null) {
            accumulator.addGrantedColor(becomes.color());
            accumulator.setColorOverriding(true);
        }
    }
}
