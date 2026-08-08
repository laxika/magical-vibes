package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect;

@CardRegistration(set = "BOK", collectorNumber = "94")
public class AuraBarbs extends Card {

    public AuraBarbs() {
        // Each enchantment deals 2 damage to its controller, then each Aura attached to a creature
        // deals 2 damage to the creature it's attached to.
        addEffect(EffectSlot.SPELL,
                new EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect(2));
    }
}
