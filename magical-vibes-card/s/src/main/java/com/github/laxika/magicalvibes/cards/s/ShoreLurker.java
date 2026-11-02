package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "34")
public class ShoreLurker extends Card {

    public ShoreLurker() {
        // When this creature enters, surveil 1.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));
    }
}
