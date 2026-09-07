package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "95")
public class Fell extends Card {

    public Fell() {
        // Destroy target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
