package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetSelfBaseToughnessFromTargetPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "107")
@CardRegistration(set = "LEG", collectorNumber = "294")
public class Sentinel extends Card {

    public Sentinel() {
        var inCombatWithThis = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentInCombatWithSourcePredicate()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new SetSelfBaseToughnessFromTargetPowerEffect(1, inCombatWithThis)),
                "{0}: Change this creature's base toughness to 1 plus the power of target creature blocking or blocked by this creature.",
                new PermanentPredicateTargetFilter(
                        inCombatWithThis,
                        "Target must be a creature blocking or blocked by this creature"
                )
        ));
    }
}
