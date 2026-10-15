package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

@CardRegistration(set = "INV", collectorNumber = "288")
public class VoraciousCobra extends Card {

    public VoraciousCobra() {
        // Whenever this creature deals combat damage to a creature, destroy that creature.
        // The damaged creature is baked as targetId by ON_COMBAT_DAMAGE_TO_CREATURE (non-targeting).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new DestroyTargetPermanentEffect());
    }
}
