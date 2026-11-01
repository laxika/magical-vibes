package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "188")
public class PerimeterPatrol extends Card {

    public PerimeterPatrol() {
        // Whenever an artifact you control enters, this creature gets +1/+0 until end of turn.
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(1, 0));
    }
}
