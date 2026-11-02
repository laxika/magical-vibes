package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "80")
public class KoalaSheep extends Card {

    public KoalaSheep() {
        // When this creature enters, you gain 3 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
