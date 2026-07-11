package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "LRW", collectorNumber = "16")
public class GoldmeadowDodger extends Card {

    public GoldmeadowDodger() {
        // Goldmeadow Dodger can't be blocked by creatures with power 4 or greater.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentPowerAtMostPredicate(3),
                "creatures with power 3 or less"
        ));
    }
}
