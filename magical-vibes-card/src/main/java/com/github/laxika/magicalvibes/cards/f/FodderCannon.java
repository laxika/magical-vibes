package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "302")
public class FodderCannon extends Card {

    public FodderCannon() {
        // {4}, {T}, Sacrifice a creature: This artifact deals 4 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToTargetCreatureEffect(4)
                ),
                "{4}, {T}, Sacrifice a creature: Fodder Cannon deals 4 damage to target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
