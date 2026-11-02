package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "103")
@CardRegistration(set = "LTC", collectorNumber = "176")
@CardRegistration(set = "MKC", collectorNumber = "82")
@CardRegistration(set = "C16", collectorNumber = "4")
public class SelflessSquire extends Card {

    public SelflessSquire() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), PreventDamageEffect.allToController());
        addTrigger(Triggers.damageToYouPrevented(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
