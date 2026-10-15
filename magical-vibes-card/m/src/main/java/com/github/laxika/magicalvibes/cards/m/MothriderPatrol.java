package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "30")
public class MothriderPatrol extends Card {

    public MothriderPatrol() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
