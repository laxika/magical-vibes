package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "236")
@CardRegistration(set = "HBG", collectorNumber = "251")
public class TrelasarraMoonDancer extends Card {

    public TrelasarraMoonDancer() {
        addTrigger(Triggers.youGainLife(), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ScryEffect(1)));
    }
}
