package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "80")
public class Torture extends Card {

    public Torture() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ONE_MINUS_ONE)),
                "{1}{B}: Put a -1/-1 counter on enchanted creature."
        ));
    }
}
