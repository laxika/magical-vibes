package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "77")
@CardRegistration(set = "2XM", collectorNumber = "95")
public class GlazeFiend extends Card {

    public GlazeFiend() {
        // Whenever another artifact you control enters, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), new BoostSelfEffect(2, 2));
    }
}
