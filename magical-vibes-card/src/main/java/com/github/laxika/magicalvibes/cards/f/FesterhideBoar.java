package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ISD", collectorNumber = "179")
public class FesterhideBoar extends Card {

    public FesterhideBoar() {
        // Morbid — Festerhide Boar enters the battlefield with two +1/+1 counters on it
        // if a creature died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Morbid(), 
                new PutCountersOnSourceEffect(1, 1, 2)
        ));
    }
}
