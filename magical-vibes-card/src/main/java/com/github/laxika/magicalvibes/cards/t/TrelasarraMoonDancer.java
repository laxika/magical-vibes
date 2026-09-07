package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFR", collectorNumber = "236")
public class TrelasarraMoonDancer extends Card {

    public TrelasarraMoonDancer() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ScryEffect(1)));
    }
}
