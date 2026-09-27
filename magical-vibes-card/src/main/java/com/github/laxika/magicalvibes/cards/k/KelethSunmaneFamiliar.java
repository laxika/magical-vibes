package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;

@CardRegistration(set = "CMM", collectorNumber = "33")
public class KelethSunmaneFamiliar extends Card {

    public KelethSunmaneFamiliar() {
        // Whenever a commander you control attacks, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCommanderPredicate(),
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentIsTriggeringPermanentPredicate())));
    }
}
