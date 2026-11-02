package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "S99", collectorNumber = "14")
public class DevoutMonk extends Card {

    public DevoutMonk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));
    }
}
