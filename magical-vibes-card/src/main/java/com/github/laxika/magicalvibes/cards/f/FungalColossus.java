package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "EOE", collectorNumber = "184")
public class FungalColossus extends Card {

    public FungalColossus() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new DistinctPermanentNamesCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
