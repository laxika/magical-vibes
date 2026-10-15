package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "150")
public class NotoriousAssassin extends Card {

    public NotoriousAssassin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DestroyTargetPermanentEffect(true)
                ),
                "{2}{B}, {T}, Discard a card: Destroy target nonblack creature. It can't be regenerated.",
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
