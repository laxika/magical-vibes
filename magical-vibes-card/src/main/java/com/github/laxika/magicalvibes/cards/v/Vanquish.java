package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5DN", collectorNumber = "20")
public class Vanquish extends Card {

    public Vanquish() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsBlockingPredicate(),
                "Target must be a blocking creature"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
