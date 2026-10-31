package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEitherTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "84")
public class WickedSlumber extends Card {

    public WickedSlumber() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new PutCounterOnEitherTargetPermanentEffect(CounterType.STUN))
                .addSpellEffect(new PutCounterOnEitherTargetPermanentEffect(CounterType.STUN));
    }
}
