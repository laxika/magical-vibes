package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "26")
public class AuraThief extends Card {

    public AuraThief() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new GainControlOfAllPermanentsMatchingEffect(new PermanentIsEnchantmentPredicate()));
    }
}
