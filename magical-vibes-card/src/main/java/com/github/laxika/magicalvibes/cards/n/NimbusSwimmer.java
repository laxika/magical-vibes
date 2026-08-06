package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "GTC", collectorNumber = "181")
public class NimbusSwimmer extends Card {

    public NimbusSwimmer() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
