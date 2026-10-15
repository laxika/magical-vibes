package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "175")
public class BlasterMage extends Card {

    public BlasterMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DiscardCardTypeCost(null, null), new DestroyTargetPermanentEffect()),
                "{R}, {T}, Discard a card: Destroy target Wall.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Target must be a Wall"
                )
        ));
    }
}
