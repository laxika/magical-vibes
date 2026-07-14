package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "6ED", collectorNumber = "113")
public class Blight extends Card {

    public Blight() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ));
        // When enchanted land becomes tapped, destroy it.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, new DestroyEnchantedPermanentEffect());
    }
}
