package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "161")
public class StingSlinger extends Card {

    public StingSlinger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        DealDamageEffect.to(Subjects.eachOpponent(), 2)),
                "{1}{R}, {T}, Blight 1: This creature deals 2 damage to each opponent."
        ));
    }
}
