package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;

@CardRegistration(set = "ORI", collectorNumber = "234")
public class OrbsOfWarding extends Card {

    public OrbsOfWarding() {
        // "You have hexproof."
        addEffect(EffectSlot.STATIC, new GrantControllerHexproofEffect());
        // "If a creature would deal damage to you, prevent 1 of that damage."
        addEffect(EffectSlot.STATIC, PreventFixedDamagePerSourceToControllerEffect.fromCreatures(1));
    }
}
