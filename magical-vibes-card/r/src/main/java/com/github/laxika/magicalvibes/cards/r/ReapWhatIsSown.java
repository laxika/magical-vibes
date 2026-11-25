package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "154")
public class ReapWhatIsSown extends Card {

    public ReapWhatIsSown() {
        target(TargetFilters.creature(), 0, 3)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
