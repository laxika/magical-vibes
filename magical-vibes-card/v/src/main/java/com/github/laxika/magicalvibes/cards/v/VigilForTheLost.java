package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "26")
public class VigilForTheLost extends Card {

    public VigilForTheLost() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new PayXManaGainXLifeEffect());
    }
}
