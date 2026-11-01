package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "116")
public class Attercop extends Card {

    public Attercop() {
        // Whenever a land you control enters, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(1, 1));
    }
}
