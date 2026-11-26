package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "74")
public class LashOfMalice extends Card {

    public LashOfMalice() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(2, -2));
    }
}
