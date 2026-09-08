package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaAnyNumberOfTimesPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MID", collectorNumber = "25")
public class IntrepidAdversary extends Card {

    public IntrepidAdversary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayManaAnyNumberOfTimesPutCountersOnSelfEffect("{1}{W}", CounterType.VALOR));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES, CounterType.VALOR));
    }
}
