package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "178")
@CardRegistration(set = "LCI", collectorNumber = "408")
public class CenoteScout extends Card {

    public CenoteScout() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExploreEffect());
    }
}
