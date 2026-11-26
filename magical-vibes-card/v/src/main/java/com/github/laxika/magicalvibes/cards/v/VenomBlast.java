package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPE", collectorNumber = "20")
public class VenomBlast extends Card {

    public VenomBlast() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2));
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(DealDamageEffect.bite());
    }
}
