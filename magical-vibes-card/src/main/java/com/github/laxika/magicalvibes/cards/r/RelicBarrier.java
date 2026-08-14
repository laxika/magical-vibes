package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "147")
public class RelicBarrier extends Card {

    public RelicBarrier() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Tap target artifact.",
                TargetFilters.artifact()
        ));
    }
}
