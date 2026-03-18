package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "36")
public class LumengridDrake extends Card {

    public LumengridDrake() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MetalcraftConditionalEffect(
                                new ReturnTargetPermanentToHandEffect()));
    }
}
