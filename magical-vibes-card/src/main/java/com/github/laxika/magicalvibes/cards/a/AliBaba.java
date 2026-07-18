package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "175")
public class AliBaba extends Card {

    public AliBaba() {
        // {R}: Tap target Wall.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{R}: Tap target Wall.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Target must be a Wall"
                )
        ));
    }
}
