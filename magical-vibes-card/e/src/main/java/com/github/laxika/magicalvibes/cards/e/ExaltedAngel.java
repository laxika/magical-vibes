package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "28")
@CardRegistration(set = "V15", collectorNumber = "8")
@CardRegistration(set = "MKC", collectorNumber = "63")
public class ExaltedAngel extends Card {

    public ExaltedAngel() {
        addMorph("{2}{W}{W}");
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
