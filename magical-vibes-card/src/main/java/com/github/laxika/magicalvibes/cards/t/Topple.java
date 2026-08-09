package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "NEM", collectorNumber = "24")
public class Topple extends Card {

    public Topple() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasGreatestPowerAmongAllCreaturesPredicate(),
                "Target must be a creature with the greatest power among creatures on the battlefield."
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
