package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ISD", collectorNumber = "202")
public class SomberwaldSpider extends Card {

    public SomberwaldSpider() {
        // Morbid — Somberwald Spider enters the battlefield with two +1/+1 counters on it
        // if a creature died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MorbidConditionalEffect(
                new PutCountersOnSourceEffect(1, 1, 2)
        ));
    }
}
