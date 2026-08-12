package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "328")
public class EngulfingEruption extends Card {

    public EngulfingEruption() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
