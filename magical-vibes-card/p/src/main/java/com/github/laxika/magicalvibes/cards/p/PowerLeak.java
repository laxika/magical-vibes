package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerMayPayToPreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "92")
@CardRegistration(set = "SUM", collectorNumber = "73")
@CardRegistration(set = "3ED", collectorNumber = "73")
@CardRegistration(set = "2ED", collectorNumber = "72")
@CardRegistration(set = "LEA", collectorNumber = "71")
@CardRegistration(set = "LEB", collectorNumber = "72")
public class PowerLeak extends Card {

    public PowerLeak() {
        // Enchant enchantment. At the beginning of the upkeep of enchanted enchantment's controller,
        // that player may pay any amount of mana. This Aura deals 2 damage to that player. Prevent X
        // of that damage, where X is the amount of mana that player paid this way.
        target(TargetFilters.enchantment()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new EnchantedControllerMayPayToPreventDamageEffect(2));
    }
}
