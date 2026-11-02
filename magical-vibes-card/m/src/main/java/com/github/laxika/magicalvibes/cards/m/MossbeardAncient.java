package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "173")
public class MossbeardAncient extends Card {

    public MossbeardAncient() {
        // When this creature enters, you gain 5 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));
    }
}
