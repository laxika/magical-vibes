package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "12")
public class FanBearer extends Card {

    public FanBearer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
