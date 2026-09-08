package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantmentAlterationEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreatureOrLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "CHR", collectorNumber = "19")
public class EnchantmentAlteration extends Card {

    public EnchantmentAlteration() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAuraAttachedToCreatureOrLandPredicate(),
                "Target must be an Aura attached to a creature or land"))
                .addEffect(EffectSlot.SPELL, new EnchantmentAlterationEffect());
    }
}
