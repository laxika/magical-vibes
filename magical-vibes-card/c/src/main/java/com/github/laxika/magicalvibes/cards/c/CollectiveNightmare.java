package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "95")
public class CollectiveNightmare extends Card {

    public CollectiveNightmare() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(-3, -3));
    }
}
