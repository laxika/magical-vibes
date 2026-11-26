package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "31")
public class BlightGrenade extends Card {

    public BlightGrenade() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(BoostEffect.allCreatures(-3, -3));
    }
}
