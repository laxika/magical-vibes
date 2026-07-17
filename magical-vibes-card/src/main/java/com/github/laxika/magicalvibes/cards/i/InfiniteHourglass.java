package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "378")
public class InfiniteHourglass extends Card {

    public InfiniteHourglass() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.TIME));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ALL_CREATURES, CounterType.TIME));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new RemoveCounterFromSourceEffect(CounterType.TIME, 1)),
                "{3}: Remove a time counter from Infinite Hourglass. Any player may activate this ability but only during any upkeep step.",
                ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP
        ).withActivatableByAnyPlayer());
    }
}
