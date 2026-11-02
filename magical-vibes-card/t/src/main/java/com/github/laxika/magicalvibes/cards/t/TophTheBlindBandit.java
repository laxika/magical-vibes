package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "198")
public class TophTheBlindBandit extends Card {

    public TophTheBlindBandit() {
        target(TargetFilters.landYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new EarthbendTargetLandEffect(2));

        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new PermanentCounterSum(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentIsLandPredicate(),
                        CountScope.CONTROLLER),
                new Fixed(3)));
    }
}
