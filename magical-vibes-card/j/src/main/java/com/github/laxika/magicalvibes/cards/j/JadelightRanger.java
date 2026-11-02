package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "136")
public class JadelightRanger extends Card {

    public JadelightRanger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExploreEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExploreEffect());
    }
}
