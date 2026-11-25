package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "109")
@CardRegistration(set = "TSB", collectorNumber = "80")
@CardRegistration(set = "ARC", collectorNumber = "60")
public class HuntingMoa extends Card {

    public HuntingMoa() {
        PutCountersEffect counter =
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), counter)
                .addTrigger(Triggers.dies(ObjectRelation.THIS), counter);
        addStaticEffect(new EchoEffect("{2}{G}"));
    }
}
