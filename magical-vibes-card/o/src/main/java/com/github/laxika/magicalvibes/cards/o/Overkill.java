package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "109")
public class Overkill extends Card {

    public Overkill() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(0, -9999));
    }
}
