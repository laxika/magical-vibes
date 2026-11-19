package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "158")
public class CentaurPeacemaker extends Card {

    public CentaurPeacemaker() {
        // When this creature enters, each player gains 4 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4, Subjects.eachPlayer()));
    }
}
