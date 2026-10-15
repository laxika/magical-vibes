package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "CMM", collectorNumber = "720")
@CardRegistration(set = "CMM", collectorNumber = "753")
public class BoonOfTheSpiritRealm extends Card {

    public BoonOfTheSpiritRealm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.BLESSING));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.BLESSING));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES, CounterType.BLESSING));
    }
}
