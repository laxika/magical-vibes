package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerMayPayToPreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "4ED", collectorNumber = "92")
public class PowerLeak extends Card {

    public PowerLeak() {
        // Enchant enchantment. At the beginning of the upkeep of enchanted enchantment's controller,
        // that player may pay any amount of mana. This Aura deals 2 damage to that player. Prevent X
        // of that damage, where X is the amount of mana that player paid this way.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new EnchantedControllerMayPayToPreventDamageEffect(2));
    }
}
