package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;

@CardRegistration(set = "MSC", collectorNumber = "78")
@CardRegistration(set = "MSC", collectorNumber = "396")
public class CaptainMarvelApexAvenger extends Card {

    public CaptainMarvelApexAvenger() {
        addEffect(EffectSlot.ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE,
                new MayEffect(new PutSameCountersOnSourceEffect(),
                        "Put the same number and kind of counters on Captain Marvel?"));
    }
}
