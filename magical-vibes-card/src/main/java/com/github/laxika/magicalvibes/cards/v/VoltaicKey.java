package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "219")
@CardRegistration(set = "USG", collectorNumber = "314")
public class VoltaicKey extends Card {

    public VoltaicKey() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}, {T}: Untap target artifact.",
                TargetFilters.artifact()
        ));
    }
}
