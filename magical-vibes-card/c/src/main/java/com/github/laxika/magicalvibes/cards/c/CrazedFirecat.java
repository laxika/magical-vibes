package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLosePutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "94")
public class CrazedFirecat extends Card {

    public CrazedFirecat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new FlipUntilLosePutCountersOnSourceEffect());
    }
}
