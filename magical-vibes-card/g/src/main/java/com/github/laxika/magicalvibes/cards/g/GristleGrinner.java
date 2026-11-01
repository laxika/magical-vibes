package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "59")
@CardRegistration(set = "CST", collectorNumber = "59")
public class GristleGrinner extends Card {

    public GristleGrinner() {
        // Whenever a creature dies, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new BoostSelfEffect(2, 2));
    }
}
