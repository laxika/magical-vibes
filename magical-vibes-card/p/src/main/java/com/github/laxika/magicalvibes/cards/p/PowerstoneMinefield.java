package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "115")
public class PowerstoneMinefield extends Card {

    public PowerstoneMinefield() {
        // Whenever a creature attacks or blocks, this enchantment deals 2 damage to it.
        addTrigger(Triggers.creatureAttacks(), new DealDamageToTargetCreatureEffect(2));
        addTrigger(Triggers.creatureBlocks(), new DealDamageToTargetCreatureEffect(2));
    }
}
