package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "68")
public class IonStorm extends Card {

    public IonStorm() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                CounterType.CHARGE),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{1}{R}, Remove a +1/+1 counter or a charge counter from a permanent you control: Ion Storm deals 2 damage to any target."
        ));
    }
}
