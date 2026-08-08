package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "41")
public class MinamoSightbender extends Card {

    public MinamoSightbender() {
        // {X}, {T}: Target creature with power X or less can't be blocked this turn.
        // The paid X flows into the target filter via PermanentPowerAtMostXPredicate.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{X}, {T}: Target creature with power X or less can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostXPredicate()
                        )),
                        "Target must be a creature with power X or less"
                )));
    }
}
