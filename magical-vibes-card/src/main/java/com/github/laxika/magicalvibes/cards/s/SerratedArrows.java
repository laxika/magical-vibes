package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "110")
public class SerratedArrows extends Card {

    public SerratedArrows() {
        // This artifact enters with three arrowhead counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.ARROWHEAD, new Fixed(3)));

        // At the beginning of your upkeep, if there are no arrowhead counters on this artifact,
        // sacrifice it.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.ARROWHEAD)),
                new SacrificeSelfEffect()));

        // {T}, Remove an arrowhead counter from this artifact: Put a -1/-1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.ARROWHEAD),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)
                ),
                "{T}, Remove an arrowhead counter from this artifact: Put a -1/-1 counter on target creature."
        ));
    }
}
