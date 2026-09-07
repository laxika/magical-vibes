package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "56")
public class UnlivingPsychopath extends Card {

    public UnlivingPsychopath() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostSelfEffect(1, -1)),
                "{B}: This creature gets +1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new DestroyTargetPermanentEffect()),
                "{B}, {T}: Destroy target creature with power less than this creature's power.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerLessThanSourcePowerPredicate()
                        )),
                        "Target must be a creature with power less than this creature's power"
                )
        ));
    }
}
