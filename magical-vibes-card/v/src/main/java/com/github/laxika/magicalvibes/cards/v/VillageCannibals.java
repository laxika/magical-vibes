package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "125")
public class VillageCannibals extends Card {

    public VillageCannibals() {
        // Whenever another Human creature dies, put a +1/+1 counter on Village Cannibals.
        addTrigger(Triggers.creatureDies(new PermanentHasSubtypePredicate(CardSubtype.HUMAN), ObjectRelation.ANOTHER),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
