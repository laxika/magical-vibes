package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;

@CardRegistration(set = "DOM", collectorNumber = "138")
public class RadiatingLightning extends Card {

    public RadiatingLightning() {
        // Radiating Lightning deals 3 damage to target player and 1 damage to each creature that player controls.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(3));
        addEffect(EffectSlot.SPELL, new DealDamageToAllCreaturesTargetControlsEffect(1));
    }
}
