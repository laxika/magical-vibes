package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "119")
public class FloodwaterDam extends Card {

    public FloodwaterDam() {
        // {X}{X}{1}, {T}: Tap X target lands. The two {X} symbols make the cost 2X + 1 (handled by
        // ManaCost's X symbol count), while the paid X bounds the target count via
        // withXScaledTargets; the tap handler fans over the whole chosen target group.
        addActivatedAbility(new ActivatedAbility(true, "{X}{X}{1}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{X}{X}{1}, {T}: Tap X target lands.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Targets must be lands"
                ),
                null, null, null, List.of(), 100, 100)
                .withXScaledTargets());
    }
}
