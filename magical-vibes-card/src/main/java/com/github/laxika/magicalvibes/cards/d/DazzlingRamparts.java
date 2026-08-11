package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "6")
public class DazzlingRamparts extends Card {

    public DazzlingRamparts() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
