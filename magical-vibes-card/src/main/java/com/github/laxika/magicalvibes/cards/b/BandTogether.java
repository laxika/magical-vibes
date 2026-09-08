package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "153")
public class BandTogether extends Card {

    public BandTogether() {
        // The fixed target is declared first so the optional source group can be last in the
        // flat target list while the effect still reads the sources from group 1.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new EachTargetCreatureDealsPowerDamageToTargetCreatureEffect());
        target(TargetFilters.creatureYouControl(), 0, 2);
    }
}
