package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "70")
public class SkeletonScavengers extends Card {

    public SkeletonScavengers() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        // Pay {1} for each +1/+1 counter on this creature: Regenerate this creature. When it
        // regenerates this way, put a +1/+1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new IncreaseActivationCostEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)),
                        RegenerateEffect.withPlusOnePlusOneCounterOnRegenerate()),
                "Pay {1} for each +1/+1 counter on this creature: Regenerate this creature. When it regenerates this way, put a +1/+1 counter on it."
        ));
    }
}
