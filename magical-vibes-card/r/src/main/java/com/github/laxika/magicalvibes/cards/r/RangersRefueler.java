package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "55")
public class RangersRefueler extends Card {

    public RangersRefueler() {
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY, new DrawCardEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new AnimatePermanentsEffect(3, 3, List.of(), Set.of(), null,
                                Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.PERMANENT),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Exhaust — {4}: This Vehicle becomes an artifact creature. Put a +1/+1 counter on it."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
