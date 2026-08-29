package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "APC", collectorNumber = "115")
public class PowerstoneMinefield extends Card {

    public PowerstoneMinefield() {
        // Whenever a creature attacks or blocks, this enchantment deals 2 damage to it.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new DealDamageToTargetCreatureEffect(2));
        addEffect(EffectSlot.ON_ANY_CREATURE_BLOCKS, new DealDamageToTargetCreatureEffect(2));
    }
}
