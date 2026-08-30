package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentLifeCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "M21", collectorNumber = "164")
@CardRegistration(set = "OTJ", collectorNumber = "149")
public class TerrorOfThePeaks extends Card {

    public TerrorOfThePeaks() {
        addEffect(EffectSlot.STATIC, new IncreaseOpponentLifeCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 3));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(new ChosenPermanentPower()));
    }
}
