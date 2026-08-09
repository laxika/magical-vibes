package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "89")
public class Incendiary extends Card {

    public Incendiary() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.FUSE),
                        "Put a fuse counter on this Aura?"))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.FUSE)));
    }
}
