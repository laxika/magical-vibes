package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "126")
public class GobblingOoze extends Card {

    public GobblingOoze() {
        // {G}, Sacrifice another creature: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{G}, Sacrifice another creature: Put a +1/+1 counter on this creature."
        ));
    }
}
