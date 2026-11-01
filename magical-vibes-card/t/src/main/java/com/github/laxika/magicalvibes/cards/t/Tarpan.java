package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "330")
@CardRegistration(set = "ICE", collectorNumber = "267")
public class Tarpan extends Card {

    public Tarpan() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(1));
    }
}
