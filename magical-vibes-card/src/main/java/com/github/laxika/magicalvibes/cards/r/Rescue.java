package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "63")
public class Rescue extends Card {

    public Rescue() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you control"
        )).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
