package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "LRW", collectorNumber = "77")
public class PaperfinRascal extends Card {

    public PaperfinRascal() {
        // When this creature enters, clash with an opponent. If you win, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ClashEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
