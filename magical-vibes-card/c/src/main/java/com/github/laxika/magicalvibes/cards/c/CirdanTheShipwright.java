package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CirdanTheShipwrightEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "50")
@CardRegistration(set = "LTC", collectorNumber = "133")
@CardRegistration(set = "LTC", collectorNumber = "453")
public class CirdanTheShipwright extends Card {

    public CirdanTheShipwright() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CirdanTheShipwrightEffect());
        addTrigger(Triggers.attacks(), new CirdanTheShipwrightEffect());
    }
}
