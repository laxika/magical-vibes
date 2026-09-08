package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "203")
public class MagusOfTheCandelabra extends Card {

    public MagusOfTheCandelabra() {
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "{X}, {T}: Untap X target lands.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Targets must be lands"
                ),
                null, null, null, List.of(), 0, 100)
                .withXScaledTargets());
    }
}
