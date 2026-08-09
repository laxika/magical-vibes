package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "57")
public class DiseaseCarriers extends Card {

    public DiseaseCarriers() {
        // When this creature dies, target creature gets -2/-2 until end of turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH,
                new BoostTargetCreatureEffect(-2, -2));
    }
}
