package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M13", collectorNumber = "101")
@CardRegistration(set = "M19", collectorNumber = "110")
@CardRegistration(set = "M20", collectorNumber = "109")
@CardRegistration(set = "EMN", collectorNumber = "97")
public class Murder extends Card {

    public Murder() {
        // "Destroy target creature."
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
