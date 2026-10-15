package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "74")
public class ThisTownAintBigEnough extends Card {

    public ThisTownAintBigEnough() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(
                TargetFilters.nonlandPermanent().predicate(), 3, true));
        target(TargetFilters.nonlandPermanent(), 0, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
