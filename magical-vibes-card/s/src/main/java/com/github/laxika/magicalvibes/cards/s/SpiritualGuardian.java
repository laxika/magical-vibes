package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "27")
@CardRegistration(set = "OANA", collectorNumber = "11")
@CardRegistration(set = "ANB", collectorNumber = "21")
public class SpiritualGuardian extends Card {

    public SpiritualGuardian() {
        // When this creature enters, you gain 4 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
    }
}
