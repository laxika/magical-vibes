package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "31")
public class Disperse extends Card {

    public Disperse() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
