package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "14")
public class FrostbridgeGuard extends Card {

    public FrostbridgeGuard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
