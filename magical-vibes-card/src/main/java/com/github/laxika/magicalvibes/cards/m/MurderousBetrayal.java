package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "8ED", collectorNumber = "147")
public class MurderousBetrayal extends Card {

    public MurderousBetrayal() {
        // {B}{B}, Pay half your life, rounded up: Destroy target nonblack creature. It can't be regenerated.
        addActivatedAbility(new ActivatedAbility(
                false, "{B}{B}",
                List.of(
                        PayLifeCost.halfLife(),
                        new DestroyTargetPermanentEffect(true)
                ),
                "{B}{B}, Pay half your life, rounded up: Destroy target nonblack creature. It can't be regenerated.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                        )),
                        "Target must be a nonblack creature"
                )
        ));
    }
}
