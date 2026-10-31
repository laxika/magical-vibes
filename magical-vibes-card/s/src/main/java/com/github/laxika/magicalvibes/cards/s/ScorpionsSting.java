package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "65")
@CardRegistration(set = "OM1", collectorNumber = "67")
public class ScorpionsSting extends Card {

    public ScorpionsSting() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-3, -3));
    }
}
