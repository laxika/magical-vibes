package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "21")
public class MabelsMettle extends Card {

    public MabelsMettle() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1));
    }
}
