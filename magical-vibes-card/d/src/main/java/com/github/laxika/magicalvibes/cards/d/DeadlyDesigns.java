package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "83")
public class DeadlyDesigns extends Card {

    public DeadlyDesigns() {
        target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.sourceState(
                        new PermanentHasAtLeastCountersPredicate(CounterType.PLOT, 5)),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), DestroyTargetPermanentEffect.eachTarget())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(PutCountersEffect.onSelf(CounterType.PLOT)),
                "{2}: Put a plot counter on Deadly Designs. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
