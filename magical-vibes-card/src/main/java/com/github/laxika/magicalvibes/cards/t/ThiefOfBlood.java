package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect;

@CardRegistration(set = "C15", collectorNumber = "22")
public class ThiefOfBlood extends Card {

    public ThiefOfBlood() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE));
    }
}
