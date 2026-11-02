package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "58")
@CardRegistration(set = "FDN", collectorNumber = "318")
@CardRegistration(set = "FDN", collectorNumber = "383")
@CardRegistration(set = "FDN", collectorNumber = "426")
@CardRegistration(set = "FDN", collectorNumber = "436")
@CardRegistration(set = "FDN", collectorNumber = "460")
public class BloodthirstyConqueror extends Card {

    public BloodthirstyConqueror() {
        addTrigger(Triggers.opponentLosesLife(), new GainLifeEffect(new EventValue()));
    }
}
