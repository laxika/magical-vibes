package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DFT", collectorNumber = "146")
public class Skycrash extends Card {

    public Skycrash() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addCycling("{R}");
    }
}
