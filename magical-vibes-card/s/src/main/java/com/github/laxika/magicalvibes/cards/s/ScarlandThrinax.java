package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "123")
@CardRegistration(set = "C13", collectorNumber = "209")
public class ScarlandThrinax extends Card {

    public ScarlandThrinax() {
        // Sacrifice a creature: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Sacrifice a creature: Put a +1/+1 counter on this creature."
        ));
    }
}
