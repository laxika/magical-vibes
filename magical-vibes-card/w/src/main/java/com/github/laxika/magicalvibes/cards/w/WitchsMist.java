package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "92")
public class WitchsMist extends Card {

    public WitchsMist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new DestroyTargetPermanentEffect()),
                "{2}{B}, {T}: Destroy target creature that was dealt damage this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageThisTurnPredicate()
                        )),
                        "Target must be a creature that was dealt damage this turn"
                )
        ));
    }
}
