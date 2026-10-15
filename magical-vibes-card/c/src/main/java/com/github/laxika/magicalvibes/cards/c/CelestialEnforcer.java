package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "11")
public class CelestialEnforcer extends Card {

    public CelestialEnforcer() {
        // {1}{W}, {T}: Tap target creature. Activate only if you control a creature with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W}, {T}: Tap target creature. Activate only if you control a creature with flying.",
                TargetFilters.creature()
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                1,
                "creatures with flying"));
    }
}
