package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C16", collectorNumber = "22")
public class EvolutionaryEscalation extends Card {

    public EvolutionaryEscalation() {
        targetWithDynamicCount(new Fixed(1), TargetFilters.creatureYouControl(), 1);
        target(TargetFilters.creatureAnOpponentControls());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
    }
}
