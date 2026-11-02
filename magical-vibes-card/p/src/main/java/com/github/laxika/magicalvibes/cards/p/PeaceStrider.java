package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "119")
@CardRegistration(set = "MB1", collectorNumber = "243")
@CardRegistration(set = "2XM", collectorNumber = "280")
public class PeaceStrider extends Card {

    public PeaceStrider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
