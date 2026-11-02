package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "188")
@CardRegistration(set = "SIS", collectorNumber = "52")
public class HollowhengeScavenger extends Card {

    public HollowhengeScavenger() {
        // Morbid — When Hollowhenge Scavenger enters the battlefield,
        // if a creature died this turn, you gain 5 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new Morbid(), 
                new GainLifeEffect(5)
        ));
    }
}
