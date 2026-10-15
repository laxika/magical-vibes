package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "44")
@CardRegistration(set = "C13", collectorNumber = "61")
@CardRegistration(set = "CMA", collectorNumber = "42")
public class ThornwindFaeries extends Card {

    public ThornwindFaeries() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Thornwind Faeries deals 1 damage to any target."));
    }
}
