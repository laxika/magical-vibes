package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "34")
@CardRegistration(set = "DDN", collectorNumber = "49")
@CardRegistration(set = "MM3", collectorNumber = "14")
public class LoneMissionary extends Card {

    public LoneMissionary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
    }
}
