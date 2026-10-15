package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ZNR", collectorNumber = "11")
public class EmeriaCaptain extends Card {

    public EmeriaCaptain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new PartySize()));
    }
}
