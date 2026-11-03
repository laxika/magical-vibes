package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "158")
@CardRegistration(set = "TMT", collectorNumber = "200")
@CardRegistration(set = "TMT", collectorNumber = "247")
public class MikeyLeoChaosOrder extends Card {

    public MikeyLeoChaosOrder() {
        addTrigger(Triggers.youPutCountersOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DrawCardEffect(1));
    }
}
