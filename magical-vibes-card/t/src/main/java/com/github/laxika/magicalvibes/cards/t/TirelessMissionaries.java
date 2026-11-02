package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "36")
@CardRegistration(set = "M15", collectorNumber = "39")
public class TirelessMissionaries extends Card {

    public TirelessMissionaries() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
