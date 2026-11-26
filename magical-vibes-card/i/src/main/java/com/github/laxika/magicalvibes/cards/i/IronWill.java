package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "10")
public class IronWill extends Card {

    public IronWill() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(0, 4));
        addCycling("{2}");
    }
}
