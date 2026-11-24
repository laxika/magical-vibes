package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "VIS", collectorNumber = "151")
public class PhyrexianMarauder extends Card {

    public PhyrexianMarauder() {
        // This creature enters with X +1/+1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        // This creature can't block.
        addStaticEffect(new CantBlockEffect());
        // This creature can't attack unless you pay {1} for each +1/+1 counter on it.
        addStaticEffect(CantAttackOrBlockUnlessPaysEffect.attacking(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
