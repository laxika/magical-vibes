package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "150")
public class TalonOfPain extends Card {

    public TalonOfPain() {
        addTrigger(Triggers.sourceYouControlDealsDamageToOpponent(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(
                        new TriggeringPermanentMatches(new PermanentIsSourcePermanentPredicate()))),
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.CHARGE),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{X}, {T}, Remove X charge counters from this artifact: It deals X damage to any target."
        ));
    }
}
