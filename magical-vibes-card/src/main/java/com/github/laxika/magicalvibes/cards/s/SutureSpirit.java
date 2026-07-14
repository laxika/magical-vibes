package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "16")
public class SutureSpirit extends Card {

    public SutureSpirit() {
        // {W/B}{W/B}{W/B}: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W/B}{W/B}{W/B}",
                List.of(new RegenerateEffect(true)),
                "{W/B}{W/B}{W/B}: Regenerate target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
