package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "66")
public class RookieMistake extends Card {

    public RookieMistake() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(0, 2));
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-2, 0));
    }
}
