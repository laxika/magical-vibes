package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkersWithLoyaltyBecomeCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "5")
@CardRegistration(set = "MAT", collectorNumber = "55")
@CardRegistration(set = "MAT", collectorNumber = "105")
@CardRegistration(set = "MAT", collectorNumber = "153")
@CardRegistration(set = "MAT", collectorNumber = "229")
public class SparkRupture extends Card {

    public SparkRupture() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addStaticEffect(new PlaneswalkersWithLoyaltyBecomeCreaturesEffect());
    }
}
