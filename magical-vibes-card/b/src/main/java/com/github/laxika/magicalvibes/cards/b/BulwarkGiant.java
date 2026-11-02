package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "7")
@CardRegistration(set = "RVR", collectorNumber = "11")
public class BulwarkGiant extends Card {

    public BulwarkGiant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));
    }
}
