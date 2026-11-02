package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "104")
public class SengirConnoisseur extends Card {

    public SengirConnoisseur() {
        // Whenever one or more other creatures die, put a +1/+1 counter on this creature.
        // This ability triggers only once each turn.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
