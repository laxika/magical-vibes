package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "POR", collectorNumber = "15")
public class FleetFootedMonk extends Card {

    public FleetFootedMonk() {
        // Fleet-Footed Monk can't be blocked by creatures with power 2 or greater.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentPowerAtMostPredicate(1),
                "creatures with power 1 or less"
        ));
    }
}
