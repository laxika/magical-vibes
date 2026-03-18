package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M11", collectorNumber = "38")
public class WarPriestOfThune extends Card {

    public WarPriestOfThune() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target enchantment?"));
    }
}
