package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "40K", collectorNumber = "90")
public class Clamavus extends Card {

    public Clamavus() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.ALL_OWN_CREATURES, null, true));
    }
}
