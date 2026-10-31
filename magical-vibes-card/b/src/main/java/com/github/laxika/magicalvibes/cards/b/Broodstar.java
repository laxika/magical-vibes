package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "31")
@CardRegistration(set = "HOP", collectorNumber = "8")
@CardRegistration(set = "FDC", collectorNumber = "60")
public class Broodstar extends Card {

    public Broodstar() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new ReduceOwnCastCostEffect(artifactsYouControl));
        addStaticEffect(new SetPowerToughnessToAmountEffect(artifactsYouControl, artifactsYouControl));
    }
}
