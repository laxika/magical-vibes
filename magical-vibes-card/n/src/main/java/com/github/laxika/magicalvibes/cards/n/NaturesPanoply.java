package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "131")
public class NaturesPanoply extends Card {

    public NaturesPanoply() {
        setAdditionalManaCostPerExtraTarget("{2}{G}");
        target(TargetFilters.creature(), 0, 99)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
