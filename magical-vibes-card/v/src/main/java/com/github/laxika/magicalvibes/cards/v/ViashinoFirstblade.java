package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "113")
public class ViashinoFirstblade extends Card {

    public ViashinoFirstblade() {
        // When this creature enters, it gets +2/+2 until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostSelfEffect(2, 2));
    }
}
