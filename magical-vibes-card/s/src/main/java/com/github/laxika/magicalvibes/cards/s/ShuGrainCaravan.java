package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "26")
public class ShuGrainCaravan extends Card {

    public ShuGrainCaravan() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
