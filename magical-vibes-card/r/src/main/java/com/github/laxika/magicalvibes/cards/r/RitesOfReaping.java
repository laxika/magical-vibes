package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "191")
public class RitesOfReaping extends Card {

    public RitesOfReaping() {
        // Target creature gets +3/+3 until end of turn.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(3, 3));
        // Another target creature gets -3/-3 until end of turn. (distinct by default)
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-3, -3));
    }
}
