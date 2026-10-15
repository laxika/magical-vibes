package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "15")
public class CrowdFavorites extends Card {

    public CrowdFavorites() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}{W}: Tap target creature.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new BoostSelfEffect(0, 5)),
                "{3}{W}: This creature gets +0/+5 until end of turn."));
    }
}
