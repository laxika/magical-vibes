package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Target creature gets -5/-5 until end of turn.
 */
@CardRegistration(set = "BFZ", collectorNumber = "108")
@CardRegistration(set = "DDR", collectorNumber = "43")
public class DemonsGrasp extends Card {

    public DemonsGrasp() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-5, -5));
        target(TargetFilters.creature());
    }
}
