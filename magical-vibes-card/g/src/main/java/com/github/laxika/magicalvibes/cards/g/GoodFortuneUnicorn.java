package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "240")
@CardRegistration(set = "SLD", collectorNumber = "940")
@CardRegistration(set = "SLD", collectorNumber = "1711")
@CardRegistration(set = "MH1", collectorNumber = "201")
@CardRegistration(set = "MOC", collectorNumber = "326")
public class GoodFortuneUnicorn extends Card {

    public GoodFortuneUnicorn() {
        // Whenever another creature you control enters, put a +1/+1 counter on that creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(1, false));
    }
}
