package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "321")
public class DarkRemedy extends Card {

    public DarkRemedy() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(1, 3));
    }
}
