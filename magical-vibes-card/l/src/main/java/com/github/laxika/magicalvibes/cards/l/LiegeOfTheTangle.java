package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "123")
@CardRegistration(set = "2XM", collectorNumber = "174")
public class LiegeOfTheTangle extends Card {

    public LiegeOfTheTangle() {
        target(TargetFilters.landYouControl(), 0, 99)
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new PutCountersEffect(CounterType.AWAKENING));
    }
}
