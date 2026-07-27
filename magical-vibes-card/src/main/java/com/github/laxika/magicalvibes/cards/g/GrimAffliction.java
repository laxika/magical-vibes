package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NPH", collectorNumber = "63")
public class GrimAffliction extends Card {

    public GrimAffliction() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE))
          .addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
