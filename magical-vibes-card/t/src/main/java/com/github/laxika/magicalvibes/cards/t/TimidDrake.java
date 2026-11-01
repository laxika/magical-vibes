package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "54")
@CardRegistration(set = "MMQ", collectorNumber = "111")
public class TimidDrake extends Card {

    public TimidDrake() {
        // When another creature enters, return this creature to its owner's hand.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER), ReturnToHandEffect.self());
    }
}
