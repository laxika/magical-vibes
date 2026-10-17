package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "122")
public class NayaSojourners extends Card {

    public NayaSojourners() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_DEATH, new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        "Put a +1/+1 counter on target creature?"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        "Put a +1/+1 counter on target creature?"));

        addCycling("{2}{G}");
    }
}
