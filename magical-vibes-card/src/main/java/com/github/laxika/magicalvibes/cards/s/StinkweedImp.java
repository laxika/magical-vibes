package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;

@CardRegistration(set = "RAV", collectorNumber = "107")
@CardRegistration(set = "DDC", collectorNumber = "36")
@CardRegistration(set = "DDJ", collectorNumber = "54")
@CardRegistration(set = "DVD", collectorNumber = "36")
@CardRegistration(set = "MMA", collectorNumber = "98")
public class StinkweedImp extends Card {

    public StinkweedImp() {
        // Whenever this creature deals combat damage to a creature, destroy that creature.
        // The damaged creature is baked as targetId by ON_COMBAT_DAMAGE_TO_CREATURE (non-targeting).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(5));
    }
}
