package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "WAR", collectorNumber = "190")
public class Despark extends Card {

    public Despark() {
        target(new PermanentPredicateTargetFilter(
                new PermanentMinManaValuePredicate(4),
                "Target must be a permanent with mana value 4 or greater"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
