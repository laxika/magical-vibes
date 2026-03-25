package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourceToughnessToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "39")
public class SteadfastArmasaur extends Card {

    public SteadfastArmasaur() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new DealDamageEqualToSourceToughnessToTargetCreatureEffect()),
                "{1}{W}, {T}: Steadfast Armasaur deals damage equal to its toughness to target creature blocking or blocked by it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentInCombatWithSourcePredicate()
                        )),
                        "Target must be a creature blocking or blocked by this creature"
                )
        ));
    }
}
