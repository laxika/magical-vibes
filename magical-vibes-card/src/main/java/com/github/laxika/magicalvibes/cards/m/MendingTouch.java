package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "44")
public class MendingTouch extends Card {

    public MendingTouch() {
        // Regenerate target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new RegenerateEffect(true));
    }
}
