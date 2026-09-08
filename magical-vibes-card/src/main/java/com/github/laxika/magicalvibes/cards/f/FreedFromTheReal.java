package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "38")
public class FreedFromTheReal extends Card {

    public FreedFromTheReal() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new TapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{U}: Tap enchanted creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{U}: Untap enchanted creature."
        ));
    }
}
