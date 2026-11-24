package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "85")
public class PhyrexianHydra extends Card {

    public PhyrexianHydra() {
        addStaticEffect(PreventDamageAndAddPlusCountersEffect.preventingAndPuttingPerPrevented(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
