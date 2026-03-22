package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "178")
public class BlossomDryad extends Card {

    public BlossomDryad() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapTargetPermanentEffect()),
                "{T}: Untap target land.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
