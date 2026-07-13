package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "109")
public class AbyssalHunter extends Card {

    public AbyssalHunter() {
        // {B}, {T}: Tap target creature. This creature deals damage equal to
        // its power to that creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new DealDamageToTargetCreatureEffect(new SourcePower())
                ),
                "{B}, {T}: Tap target creature. This creature deals damage equal to its power to that creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
