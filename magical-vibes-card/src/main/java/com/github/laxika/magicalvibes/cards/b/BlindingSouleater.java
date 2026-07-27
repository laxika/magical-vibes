package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "131")
public class BlindingSouleater extends Card {

    public BlindingSouleater() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W/P}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W/P}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
