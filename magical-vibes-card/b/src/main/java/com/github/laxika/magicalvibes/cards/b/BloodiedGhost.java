package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "EVE", collectorNumber = "83")
public class BloodiedGhost extends Card {

    public BloodiedGhost() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(1)));
    }
}
