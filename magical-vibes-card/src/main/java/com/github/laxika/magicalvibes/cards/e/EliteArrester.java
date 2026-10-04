package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "266")
public class EliteArrester extends Card {

    public EliteArrester() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{U}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
