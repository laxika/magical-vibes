package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "156")
public class GlowstoneRecluse extends Card {

    public GlowstoneRecluse() {
        addTrigger(Triggers.mutates(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
