package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreaturesWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "110")
public class ExperimentKraj extends Card {

    public ExperimentKraj() {
        addEffect(EffectSlot.STATIC,
                new GainActivatedAbilitiesOfCreaturesWithCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
