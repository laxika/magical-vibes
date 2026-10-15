package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "28")
public class RazorfinAbolisher extends Card {

    public RazorfinAbolisher() {
        // {1}{U}, {T}: Return target creature with a counter on it to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}, {T}: Return target creature with a counter on it to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasCountersPredicate(CounterType.ANY)
                        )),
                        "Target must be a creature with a counter on it")));
    }
}
