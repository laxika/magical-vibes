package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "222")
public class ObekaSplitterOfSeconds extends Card {

    public ObekaSplitterOfSeconds() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new AdditionalUpkeepStepsEffect(new EventValue()));
    }
}
