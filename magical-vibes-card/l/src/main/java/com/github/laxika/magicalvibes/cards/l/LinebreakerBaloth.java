package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "DMU", collectorNumber = "168")
public class LinebreakerBaloth extends Card {

    public LinebreakerBaloth() {
        // Linebreaker Baloth can't be blocked by creatures with power 2 or less.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentPowerAtLeastPredicate(3),
                "creatures with power 3 or greater"
        ));
    }
}
