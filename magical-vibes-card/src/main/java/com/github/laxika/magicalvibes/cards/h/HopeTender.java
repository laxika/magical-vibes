package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "119")
public class HopeTender extends Card {

    public HopeTender() {
        PermanentPredicateTargetFilter landFilter = new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        );

        // {1}, {T}: Untap target land.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}, {T}: Untap target land.",
                landFilter
        ));

        // {1}, {T}, Exert this creature: Untap two target lands.
        // Exert ("won't untap during your next untap step") is SkipNextUntapEffect(SELF),
        // matching Steward of Solidarity / Fervent Paincaster / Angel of Condemnation.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)
                ),
                "{1}, {T}, Exert this creature: Untap two target lands.",
                List.of(landFilter, landFilter),
                2, 2
        ));
    }
}
