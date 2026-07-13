package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SHM", collectorNumber = "230")
public class MedicineRunner extends Card {

    public MedicineRunner() {
        // When this creature enters, you may remove a counter from target permanent.
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new RemoveCounterFromTargetPermanentEffect(),
                                "Remove a counter from target permanent?"));
    }
}
