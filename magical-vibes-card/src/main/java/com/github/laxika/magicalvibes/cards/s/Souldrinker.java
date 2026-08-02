package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "158")
public class Souldrinker extends Card {

    public Souldrinker() {
        // Pay 3 life: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(3), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Pay 3 life: Put a +1/+1 counter on this creature."
        ));
    }
}
