package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "28")
public class BeguilerOfWills extends Card {

    public BeguilerOfWills() {
        // {T}: Gain control of target creature with power less than or equal to
        // the number of creatures you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfTargetPermanentEffect()),
                "{T}: Gain control of target creature with power less than or equal to the number of creatures you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostControlledCreatureCountPredicate()
                        )),
                        "Target must be a creature with power less than or equal to the number of creatures you control"
                )
        ));
    }
}
