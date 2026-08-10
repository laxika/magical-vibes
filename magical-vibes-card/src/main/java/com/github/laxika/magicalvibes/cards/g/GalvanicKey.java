package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "173")
public class GalvanicKey extends Card {

    public GalvanicKey() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}, {T}: Untap target artifact.",
                TargetFilters.artifact()
        ));
    }
}
