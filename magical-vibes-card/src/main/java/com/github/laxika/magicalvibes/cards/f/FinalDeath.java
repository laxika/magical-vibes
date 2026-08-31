package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THB", collectorNumber = "95")
public class FinalDeath extends Card {

    public FinalDeath() {
        // Exile target creature.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
