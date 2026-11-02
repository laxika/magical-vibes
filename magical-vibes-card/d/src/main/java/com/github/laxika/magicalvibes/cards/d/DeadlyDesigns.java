package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "83")
public class DeadlyDesigns extends Card {

    public DeadlyDesigns() {
        target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.state(),
                new StateTriggerEffect(
                        new PermanentHasAtLeastCountersPredicate(CounterType.PLOT, 5),
                        List.of(new SacrificeSelfThenEffect(new DestroyEachTargetPermanentEffect())),
                        "Deadly Designs's state-triggered ability"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLOT)),
                "{2}: Put a plot counter on Deadly Designs. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
