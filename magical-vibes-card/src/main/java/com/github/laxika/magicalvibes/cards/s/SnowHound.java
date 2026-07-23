package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "53")
public class SnowHound extends Card {

    public SnowHound() {
        // {1}, {T}: Return this creature and target green or blue creature you control to their owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(ReturnToHandEffect.self(), ReturnToHandEffect.target()),
                "{1}, {T}: Return this creature and target green or blue creature you control to their owner's hand.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.BLUE))
                        )),
                        "Target must be a green or blue creature you control")));
    }
}
