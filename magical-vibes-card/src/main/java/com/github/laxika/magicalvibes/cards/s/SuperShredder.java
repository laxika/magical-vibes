package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "TMT", collectorNumber = "83")
@CardRegistration(set = "TMT", collectorNumber = "217")
@CardRegistration(set = "TMT", collectorNumber = "285")
@CardRegistration(set = "TMT", collectorNumber = "295")
public class SuperShredder extends Card {

    public SuperShredder() {
        // Whenever another permanent leaves the battlefield, put a +1/+1 counter on Super Shredder.
        addEffect(EffectSlot.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
