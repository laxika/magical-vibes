package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "TDM", collectorNumber = "200")
public class KinTreeSeverance extends Card {

    public KinTreeSeverance() {
        target(new PermanentPredicateTargetFilter(
                new PermanentMinManaValuePredicate(3),
                "Target must be a permanent with mana value 3 or greater"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
