package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "41")
public class ErraticMutation extends Card {

    public ErraticMutation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RevealUntilNonlandBoostTargetCreatureByManaValueEffect());
    }
}
