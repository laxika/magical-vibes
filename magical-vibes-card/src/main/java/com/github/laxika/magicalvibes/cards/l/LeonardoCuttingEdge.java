package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "TMT", collectorNumber = "15")
@CardRegistration(set = "TMT", collectorNumber = "211")
@CardRegistration(set = "TMT", collectorNumber = "281")
@CardRegistration(set = "TMT", collectorNumber = "291")
public class LeonardoCuttingEdge extends Card {

    public LeonardoCuttingEdge() {
        addSneak("{W}");
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
