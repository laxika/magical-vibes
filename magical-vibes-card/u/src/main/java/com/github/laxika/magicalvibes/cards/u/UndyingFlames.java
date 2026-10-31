package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect;

@CardRegistration(set = "SOK", collectorNumber = "119")
public class UndyingFlames extends Card {

    public UndyingFlames() {
        addSpellEffect(new ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect());
        addSpellEffect(new EpicEffect());
    }
}
