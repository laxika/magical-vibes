package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "1")
public class AegisOfTheHeavens extends Card {

    public AegisOfTheHeavens() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 7));
    }
}
