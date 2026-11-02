package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "3")
@CardRegistration(set = "C13", collectorNumber = "3")
@CardRegistration(set = "M15", collectorNumber = "2")
@CardRegistration(set = "M19", collectorNumber = "5")
@CardRegistration(set = "FDN", collectorNumber = "135")
@CardRegistration(set = "FDN", collectorNumber = "293")
@CardRegistration(set = "WAR", collectorNumber = "4")
@CardRegistration(set = "DDH", collectorNumber = "9")
@CardRegistration(set = "IMA", collectorNumber = "5")
@CardRegistration(set = "TSR", collectorNumber = "290")
public class AjanisPridemate extends Card {

    public AjanisPridemate() {
        addTrigger(Triggers.youGainLife(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
