package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "54")
public class ImpedeMomentum extends Card {

    public ImpedeMomentum() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.STUN, 3));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
