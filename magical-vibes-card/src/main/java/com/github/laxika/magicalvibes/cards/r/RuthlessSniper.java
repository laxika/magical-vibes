package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "AKH", collectorNumber = "105")
public class RuthlessSniper extends Card {

    public RuthlessSniper() {
        // Whenever you cycle or discard a card, you may pay {1}. If you do, put a -1/-1 counter on
        // target creature. Cycling is a discard (CR 702.29e), so the single controller-discard trigger
        // covers both. As with Drake Haven, the may-pay comes up at resolution; the target creature is
        // chosen there too (the resolution-time targeting path, as in Furnace Celebration).
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new MayPayManaEffect("{1}",
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        "Pay {1} to put a -1/-1 counter on target creature?"));
    }
}
