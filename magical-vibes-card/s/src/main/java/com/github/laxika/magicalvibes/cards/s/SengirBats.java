package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HML", collectorNumber = "57a")
@CardRegistration(set = "HML", collectorNumber = "57b")
public class SengirBats extends Card {

    public SengirBats() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
