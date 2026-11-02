package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YECL", collectorNumber = "28")
public class VolatileRift extends Card {

    public VolatileRift() {
        addTrigger(Triggers.putIntoHandFrom(Zone.LIBRARY, new PermanentIsCreaturePredicate(), ObjectRelation.YOU_OWN),
                new PerpetuallyBoostTriggeringCardEffect(new ColorsAmongControlledPermanents(), new Fixed(0)));
    }
}
