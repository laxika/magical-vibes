package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "46")
public class VeteranOfTheDepths extends Card {

    public VeteranOfTheDepths() {
        // Whenever this creature becomes tapped, you may put a +1/+1 counter on it.
        addTrigger(Triggers.becomesTapped(),
                new MayEffect(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE), "Put a +1/+1 counter on this creature?"));
    }
}
