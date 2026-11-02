package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "168")
@CardRegistration(set = "KLR", collectorNumber = "181")
public class SageOfShailasClaim extends Card {

    public SageOfShailasClaim() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(3));
    }
}
