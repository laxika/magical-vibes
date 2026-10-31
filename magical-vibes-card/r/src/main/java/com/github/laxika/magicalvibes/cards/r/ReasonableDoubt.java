package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "69")
public class ReasonableDoubt extends Card {

    public ReasonableDoubt() {
        addSpellEffect(new CounterUnlessPaysEffect(2));
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new SuspectEffect(GrantScope.TARGET));
    }
}
