package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "59")
public class RishadanDockhand extends Card {

    public RishadanDockhand() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET, TargetFilters.land().predicate())),
                "{1}, {T}: Tap target land.",
                TargetFilters.land()
        ));
    }
}
