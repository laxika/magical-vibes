package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "120")
public class ShapeOfTheWiitigo extends Card {

    public ShapeOfTheWiitigo() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 6))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect(
                                PermanentReference.ATTACHED, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
