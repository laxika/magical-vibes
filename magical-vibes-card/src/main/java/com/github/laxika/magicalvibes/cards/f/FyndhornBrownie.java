package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "231")
public class FyndhornBrownie extends Card {

    public FyndhornBrownie() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{G}, {T}: Untap target creature.",
                TargetFilters.creature()
        ));
    }
}
