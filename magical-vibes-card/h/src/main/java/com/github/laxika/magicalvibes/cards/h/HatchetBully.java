package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "54")
public class HatchetBully extends Card {

    public HatchetBully() {
        // {2}{R}, {T}, Put a -1/-1 counter on a creature you control: This creature deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{2}{R}, {T}, Put a -1/-1 counter on a creature you control: This creature deals 2 damage to any target."
        ));
    }
}
