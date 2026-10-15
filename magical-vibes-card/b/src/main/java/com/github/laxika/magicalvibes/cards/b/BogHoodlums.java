package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "LRW", collectorNumber = "100")
public class BogHoodlums extends Card {

    public BogHoodlums() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // When this creature enters, clash with an opponent. If you win, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ClashEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
