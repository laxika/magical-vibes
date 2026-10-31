package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NPH", collectorNumber = "63")
@CardRegistration(set = "MM2", collectorNumber = "84")
public class GrimAffliction extends Card {

    public GrimAffliction() {
        target(TargetFilters.creature()).addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE))
          .addSpellEffect(new ProliferateEffect());
    }
}
