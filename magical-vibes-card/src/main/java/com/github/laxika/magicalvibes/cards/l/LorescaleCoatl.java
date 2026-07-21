package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ARB", collectorNumber = "101")
public class LorescaleCoatl extends Card {

    public LorescaleCoatl() {
        // Whenever you draw a card, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
