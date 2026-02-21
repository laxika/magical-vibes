package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "10E", collectorNumber = "170")
public class RainOfTears extends Card {

    public RainOfTears() {
        setNeedsTarget(true);
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ));
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
