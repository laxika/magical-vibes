package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrizzledHuntmasterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "49")
public class GrizzledHuntmaster extends Card {

    public GrizzledHuntmaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GrizzledHuntmasterEffect());
    }
}
