package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "26")
@CardRegistration(set = "DST", collectorNumber = "7")
public class LoxodonMystic extends Card {

    public LoxodonMystic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
