package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOB", collectorNumber = "129")
@CardRegistration(set = "HOC", collectorNumber = "129")
public class MirkwoodPathmaker extends Card {

    public MirkwoodPathmaker() {
        PermanentCount landsYouControl =
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));
    }
}
