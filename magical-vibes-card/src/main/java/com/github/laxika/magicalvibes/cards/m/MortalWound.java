package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VIS", collectorNumber = "113")
public class MortalWound extends Card {

    public MortalWound() {
        // Enchant creature
        // When enchanted creature is dealt damage, destroy it.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE,
                        new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));
    }
}
