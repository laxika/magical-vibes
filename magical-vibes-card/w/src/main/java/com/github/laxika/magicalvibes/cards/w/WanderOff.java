package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "104")
public class WanderOff extends Card {

    public WanderOff() {
        // Exile target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
