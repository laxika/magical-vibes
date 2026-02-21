package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "14")
public class Demystify extends Card {

    public Demystify() {
        setNeedsTarget(true);
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"
        ));
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
