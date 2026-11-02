package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "51")
@CardRegistration(set = "WOC", collectorNumber = "92")
public class FaerieSeer extends Card {

    public FaerieSeer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
