package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "287")
public class Decimate extends Card {

    public Decimate() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        setAllowSharedTargets(true);
    }
}
