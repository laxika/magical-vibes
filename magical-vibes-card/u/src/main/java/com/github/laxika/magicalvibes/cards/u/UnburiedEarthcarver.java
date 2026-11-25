package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "95")
public class UnburiedEarthcarver extends Card {

    public UnburiedEarthcarver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{2}, Sacrifice another creature: Put a +1/+1 counter on Unburied Earthcarver."
        ));
    }
}
