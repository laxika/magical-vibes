package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "125")
public class VoraciousNull extends Card {

    public VoraciousNull() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "{1}{B}, Sacrifice another creature: Put two +1/+1 counters on this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
