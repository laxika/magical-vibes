package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextCombatPhaseEffect;

@CardRegistration(set = "9ED", collectorNumber = "7")
public class BlindingAngel extends Card {

    public BlindingAngel() {
        // Whenever this creature deals combat damage to a player, that player skips their next combat phase.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SkipNextCombatPhaseEffect());
    }
}
