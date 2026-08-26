package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "90")
public class RathiTrapper extends Card {

    public RathiTrapper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{B}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
