package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "93")
@CardRegistration(set = "SOI", collectorNumber = "138")
public class Throttle extends Card {

    public Throttle() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(-4, -4));
    }
}
