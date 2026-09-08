package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromControlledPermanentsToSourceEffect;

@CardRegistration(set = "KLD", collectorNumber = "71")
public class AetherbornMarauder extends Card {

    public AetherbornMarauder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MoveCountersFromControlledPermanentsToSourceEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
