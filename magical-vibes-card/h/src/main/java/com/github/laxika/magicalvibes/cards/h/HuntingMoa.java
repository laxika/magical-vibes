package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "109")
@CardRegistration(set = "TSB", collectorNumber = "80")
@CardRegistration(set = "ARC", collectorNumber = "60")
public class HuntingMoa extends Card {

    public HuntingMoa() {
        PutCounterOnTargetPermanentEffect counter =
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), counter)
                .addTrigger(Triggers.dies(ObjectRelation.THIS), counter);
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{2}{G}"));
    }
}
