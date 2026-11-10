package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "78")
@CardRegistration(set = "MSC", collectorNumber = "396")
public class CaptainMarvelApexAvenger extends Card {

    public CaptainMarvelApexAvenger() {
        addTrigger(Triggers.youPutCountersOnAnotherCreature(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new TriggeringPermanentHasSubtype(CardSubtype.KREE))),
                new MayEffect(new PutSameCountersOnSourceEffect(),
                        "Put the same number and kind of counters on Captain Marvel?"));
    }
}
