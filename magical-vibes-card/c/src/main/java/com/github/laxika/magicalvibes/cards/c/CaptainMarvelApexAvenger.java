package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "78")
@CardRegistration(set = "MSC", collectorNumber = "396")
public class CaptainMarvelApexAvenger extends Card {

    public CaptainMarvelApexAvenger() {
        addTrigger(Triggers.youPutCountersOnAnotherCreature(),
                new MayEffect(new PutSameCountersOnSourceEffect(true),
                        "Put the same number and kind of counters on Captain Marvel?"));
    }
}
