package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "RIX", collectorNumber = "175")
public class AwakenedAmalgam extends Card {

    public AwakenedAmalgam() {
        DynamicAmount differentlyNamedLands = new DistinctPermanentNamesCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(differentlyNamedLands, differentlyNamedLands));
    }
}
