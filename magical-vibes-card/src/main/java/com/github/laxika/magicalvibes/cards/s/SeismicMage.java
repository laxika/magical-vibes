package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "211")
public class SeismicMage extends Card {

    public SeismicMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DestroyTargetPermanentEffect()),
                "{2}{R}, {T}, Discard a card: Destroy target land.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
