package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ECL", collectorNumber = "59")
@CardRegistration(set = "ECL", collectorNumber = "308")
public class Mirrorform extends Card {

    public Mirrorform() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA)),
                "Target must be a non-Aura permanent."))
                .addEffect(EffectSlot.SPELL,
                        new EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect(
                                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
