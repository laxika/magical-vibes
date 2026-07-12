package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "106")
@CardRegistration(set = "9ED", collectorNumber = "102")
public class TemporalAdept extends Card {

    public TemporalAdept() {
        // {U}{U}{U}, {T}: Return target permanent to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}{U}",
                List.of(ReturnToHandEffect.target()),
                "{U}{U}{U}, {T}: Return target permanent to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent")));
    }
}
