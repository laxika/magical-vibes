package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "8")
public class DailyRegimen extends Card {

    public DailyRegimen() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new PutCounterOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{W}: Put a +1/+1 counter on enchanted creature."
        ));
    }
}
