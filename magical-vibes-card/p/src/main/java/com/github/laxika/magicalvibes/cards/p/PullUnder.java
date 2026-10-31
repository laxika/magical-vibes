package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Target creature gets -5/-5 until end of turn.
 */
@CardRegistration(set = "CHK", collectorNumber = "137")
public class PullUnder extends Card {

    public PullUnder() {
        addSpellEffect(new BoostTargetCreatureEffect(-5, -5));
        target(TargetFilters.creature());
    }
}
