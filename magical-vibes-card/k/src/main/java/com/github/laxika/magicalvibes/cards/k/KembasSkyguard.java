package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "13")
@CardRegistration(set = "DDF", collectorNumber = "13")
public class KembasSkyguard extends Card {

    public KembasSkyguard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
