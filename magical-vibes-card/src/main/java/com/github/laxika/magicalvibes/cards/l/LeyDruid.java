package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "251")
@CardRegistration(set = "5ED", collectorNumber = "308")
@CardRegistration(set = "4ED", collectorNumber = "256")
@CardRegistration(set = "SUM", collectorNumber = "206")
public class LeyDruid extends Card {

    public LeyDruid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Untap target land.",
                TargetFilters.land()
        ));
    }
}
