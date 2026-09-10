package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "41")
public class BlindingDrone extends Card {

    public BlindingDrone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{C}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{C}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
