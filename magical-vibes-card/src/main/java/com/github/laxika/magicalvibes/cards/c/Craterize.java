package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "126")
public class Craterize extends Card {

    public Craterize() {
        // Destroy target land.
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
