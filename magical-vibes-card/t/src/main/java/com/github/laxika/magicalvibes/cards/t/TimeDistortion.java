package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReverseTurnOrderEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "8")
@CardRegistration(set = "OPCA", collectorNumber = "8")
public class TimeDistortion extends Card {

    public TimeDistortion() {
        addTrigger(Triggers.encounterThis(), new ReverseTurnOrderEffect());
    }
}
